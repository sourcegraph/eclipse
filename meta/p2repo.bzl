load("@rules_java//java:defs.bzl", "JavaInfo", "java_common")

#Copied from https://github.com/bazelbuild/rules_jvm_external/blob/3414d975b3bee41e1297c7f0f5a979bba81ab92b/private/rules/coursier.bzl#L252
def _java_path(repository_ctx):
    java_home = repository_ctx.os.environ.get("JAVA_HOME")
    if java_home != None:
        return repository_ctx.path(java_home + "/bin/java")
    elif repository_ctx.which("java") != None:
        return repository_ctx.which("java")
    else:
        fail("No JAVA_HOME set and no java on PATH")

def _xpath_query(ctx, file, query):
    exec = ctx.execute([
        _java_path(ctx),
        "-cp",
        "xpath-jars/*",
        "net.sf.saxon.Query",
        "!method=adaptive",
        "-s:" + file,
        "-qs:" + query,
    ])
    if exec.return_code != 0:
        fail("Failed to execute XPath query: %s" % exec.stderr)
    return exec.stdout

def _process_site(ctx, url):
    directory_name = "metadata/" + url.replace(":", "_").replace("/", "_")
    download = ctx.download_and_extract(
        url = url + "/compositeArtifacts.jar",
        output = directory_name,
        allow_fail = True,
    )
    if not download.success:
        download = ctx.download(
            url = url + "/compositeArtifacts.xml",
            output = directory_name + "/compositeArtifacts.xml",
            allow_fail = True,
        )
    if download.success:
        subrepo = _xpath_query(ctx, directory_name + "/compositeArtifacts.xml", "string(repository/children/child/@location)")[1:-1]
        base_path = url + "/" + subrepo + "/"
    else:
        base_path = url + "/"

    ctx.download_and_extract(
        url = base_path + "content.jar",
        output = directory_name,
    )

    provided_feature_list = _xpath_query(ctx, directory_name + "/content.xml", "//unit/artifacts/artifact[@classifier='org.eclipse.update.feature']/(concat(@id, ':', @version))").splitlines()
    provided_features = {id: version for id, version in [feature[1:-1].split(":") for feature in provided_feature_list]}

    provided_plugin_list = _xpath_query(ctx, directory_name + "/content.xml", "//unit/artifacts/artifact[@classifier='osgi.bundle']/(concat(@id, ':', @version))").splitlines()
    provided_plugins = {id: version for id, version in [plugin[1:-1].split(":") for plugin in provided_plugin_list]}

    return struct(url = url, base_path = base_path, provided_features = provided_features, provided_plugins = provided_plugins)

def _make_name(id):
    return id.replace(".", "_").replace("-", "_")

def _plugin_impl(ctx):
    return [
        DefaultInfo(files = depset([ctx.file.jar])),
        JavaInfo(
            output_jar = ctx.file.jar,
            compile_jar = ctx.file.jar,
            source_jar = ctx.file.source_jar,
        ),
    ]

eclipse_plugin = rule(
    implementation = _plugin_impl,
    attrs = {
        "jar": attr.label(allow_single_file = [".jar"]),
        "source_jar": attr.label(allow_single_file = [".jar"]),
    },
    provides = [JavaInfo],
)

def _combined_impl(ctx):
    return [
        DefaultInfo(files = depset(transitive = [dep[DefaultInfo].files for dep in ctx.attr.inputs])),
        java_common.merge([dep[JavaInfo] for dep in ctx.attr.inputs]),
    ]

combined = rule(
    implementation = _combined_impl,
    attrs = {
        "inputs": attr.label_list(providers = [JavaInfo], default = []),
    },
)

def _p2impl(ctx):
    # System details for the native code
    system = "win32" if ctx.os.name.startswith("Win") else "linux" if ctx.os.name.startswith("Linux") else "macosx" if ctx.os.name.startswith("mac") else None
    arch = "x86_64" if ctx.os.arch == "amd64" else ctx.os.arch
    ws = "win32" if system == "win32" else "gtk" if system == "linux" else "cocoa" if system == "macosx" else None

    # Download the XPath jars
    ctx.download(
        url = "https://repo1.maven.org/maven2/net/sf/saxon/Saxon-HE/12.4/Saxon-HE-12.4.jar",
        output = "xpath-jars/Saxon-HE-12.4.jar",
    )

    ctx.download(
        url = "https://repo1.maven.org/maven2/org/xmlresolver/xmlresolver/5.2.2/xmlresolver-5.2.2.jar",
        output = "xpath-jars/xmlresolver-5.2.2.jar",
    )

    # Download the p2 repositories metadata
    sites = [_process_site(ctx, site) for site in ctx.attr.sites]

    # resolve feature versions in target platform
    run_config = ctx.workspace_root.get_child(ctx.attr.run_config)
    ctx.watch(run_config)
    requested_features = [f[1:-1] for f in _xpath_query(ctx, str(run_config), "//setAttribute[@key='selected_features']/setEntry/ substring-before(@value,':')").splitlines()]

    features_grouped = {site.base_path: [] for site in sites}

    for f in requested_features:
        if f not in ctx.attr.local_features:
            found = False
            for site in sites:
                if f in site.provided_features:
                    features_grouped[site.base_path].append("%s_%s" % (f, site.provided_features[f]))
                    found = True
                    break
            if not found:
                fail("Feature %s not found in any site" % f)

    feature_downloads = [
        ctx.download(
            url = "%s/features/%s.jar" % (site.base_path, f),
            output = "features/%s.jar" % f,
            block = False,
        )
        for site in sites
        for f in features_grouped[site.base_path]
    ]
    for d in feature_downloads:
        d.wait()

    #prepare for plugin resolution
    requested_plugins = {}
    standalone_plugins = []
    plugin_downloads = []
    source_downloads = []
    downloaded_sources = {}
    plugins_in_features = {feature: [] for feature in requested_features}

    def download_plugin(url, id, version):
        plugin_downloads.append(
            ctx.download(
                url = "%s/plugins/%s_%s.jar" % (url, id, version),
                output = "plugins/%s_%s.jar" % (id, version),
                block = False,
            ),
        )
        source_downloads.append(
            (
                id,
                ctx.download(
                    url = "%s/plugins/%s.source_%s.jar" % (url, id, version),
                    output = "plugins/%s_%s-sources.jar" % (id, version),
                    block = False,
                    allow_fail = True,
                ),
            ),
        )

    #resolve standalone plugin versions in target platform
    requested_standalone_plugins = [f[1:-1] for f in _xpath_query(ctx, str(run_config), "//setAttribute[@key='additional_plugins']/setEntry/ substring-before(@value,':')").splitlines()]
    for plugin in requested_standalone_plugins:
        found = False
        for site in sites:
            if plugin in site.provided_plugins:
                if plugin not in requested_plugins:
                    requested_plugins[plugin] = site.provided_plugins[plugin]
                    download_plugin(site.base_path, plugin, site.provided_plugins[plugin])
                    found = True
                    break
                elif requested_plugins[plugin] != site.provided_plugins[plugin]:
                    fail("Conflicting versions of plugin %s found: %s and %s" % (plugin, requested_plugins[plugin], site.provided_plugins[plugin]))
                else:
                    found = True
                    break
        if not found:
            fail("Plugin %s not found in any site" % plugin)

    # resolve plugin versions from features
    native_filter = "(not(@ws) or @ws = '%s') and (not(@os) or @os = '%s') and (not(@arch) or @arch = '%s')" % (ws, system, arch)

    for url, features in features_grouped.items():
        for feature in features:
            feature_name = feature.rsplit("_", 1)[0]
            ctx.extract(
                "features/%s.jar" % feature,
                "features/%s" % feature,
            )
            plugin_list = _xpath_query(ctx, "features/%s/feature.xml" % feature, "feature/plugin[%s]/(concat(@id, ':', @version))" % native_filter).splitlines()
            for plugin in plugin_list:
                id, version = plugin[1:-1].split(":")
                if id not in requested_plugins:
                    requested_plugins[id] = version
                    plugins_in_features[feature_name].append(id)
                    download_plugin(url, id, version)
                elif version != requested_plugins[id]:
                    fail("Conflicting versions of plugin %s found: %s and %s" % (id, version, requested_plugins[id]))
                else:
                    plugins_in_features[feature_name].append(id)

    for d in plugin_downloads:
        d.wait()

    for id, d in source_downloads:
        res = d.wait()
        if res.success:
            downloaded_sources[id] = ()

    # Write the build file

    buidl_header = """
package(default_visibility = ["//visibility:public"])
load("@//meta:p2repo.bzl", "eclipse_plugin", "combined")
    """

    build_plugins = [
        ("""
eclipse_plugin(
    name = "%s",
    jar = "plugins/%s_%s.jar",
    source_jar = "plugins/%s_%s-sources.jar",
)
    """ % (_make_name(id), id, version, id, version)) if id in downloaded_sources else ("""
eclipse_plugin(
    name = "%s",
    jar = "plugins/%s_%s.jar",
)
    """ % (_make_name(id), id, version))
        for id, version in requested_plugins.items()
    ]

    build_features = []
    for feature, plugins in plugins_in_features.items():
        plugins_text = ", ".join(["\":%s\"" % _make_name(p) for p in plugins])
        build_features.append("""
combined(
    name = "feature__%s",
    inputs = [ %s ],
)
        """ % (_make_name(feature), plugins_text))

    features_text = ", ".join(["\":feature__%s\"" % _make_name(f) for f in plugins_in_features.keys()])
    build_target_platform = """
combined(
    name = "target_platform",
    inputs = [ %s ],
)
    """ % features_text

    ctx.file(
        "BUILD",
        content = "\n\n".join([buidl_header] + build_plugins + build_features + [build_target_platform]),
    )

    print(ctx.path("."))
    return None

p2repo = repository_rule(
    implementation = _p2impl,
    attrs = {
        "sites": attr.string_list(mandatory = True, allow_empty = False),
        "run_config": attr.string(mandatory = True),
        "local_features": attr.string_list(mandatory = False, allow_empty = True, default = []),
    },
)
