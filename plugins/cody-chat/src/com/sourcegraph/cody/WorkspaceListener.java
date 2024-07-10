package com.sourcegraph.cody;

import static java.lang.System.out;

import com.sourcegraph.cody.protocol_generated.CodyAgentServer;
import com.sourcegraph.cody.protocol_generated.ProtocolTextDocument;
import com.sourcegraph.cody.protocol_generated.TextDocument_DidFocusParams;
import java.util.function.Consumer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class WorkspaceListener implements IPartListener2 {
  @Override
  public void partActivated(IWorkbenchPartReference partReference) {
    var file = getFile(partReference);
    if (file == null) {
      return;
    }

    withAgentServer(
        server -> {
          var params = new TextDocument_DidFocusParams();
          params.uri = file.getLocationURI().toString();
          server.textDocument_didFocus(params);
          out.println(file.getLocationURI());
        });
  }

  @Override
  public void partOpened(IWorkbenchPartReference iWorkbenchPartReference) {
    var file = getFile(iWorkbenchPartReference);
    if (file == null) {
      return;
    }

    withAgentServer(
        server -> {
          var params = new ProtocolTextDocument();
          params.uri = file.getLocationURI().toString();
          server.textDocument_didOpen(params);
          out.println(file.getLocationURI());
        });
  }

  private void withAgentServer(Consumer<CodyAgentServer> callback) {
    var agent = CodyAgent.AGENT;
    if (agent != null && agent.isRunning()) {
      callback.accept(agent.server);
    }
  }

  @Nullable
  private IFile getFile(IWorkbenchPartReference partReference) {
    var part = partReference.getPart(false);
    if (!(part instanceof ITextEditor)) {
      return null;
    }
    var editor = (ITextEditor) part;
    var input = editor.getEditorInput();
    if (!(input instanceof FileEditorInput)) {
      out.println("Unknown input kind: " + input.getClass());
      return null;
    }
    return ((FileEditorInput) input).getFile();
  }
}
