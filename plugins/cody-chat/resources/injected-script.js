function eclipse_postMessage(message) {
  console.log(`eclipse_postMessage ${message}`);
  fire(JSON.parse(message));
}

function fire(message) {
  const event = new CustomEvent("message");
  event.data = message;
  window.dispatchEvent(event);
}

// Implemented in Java:
// function eclipse_receiveMessage(message) { ... }

globalThis.acquireVsCodeApi = (function () {
  let acquired = false;
  let state = undefined;

  return () => {
    if (acquired) {
      throw new Error(
        "An instance of the VS Code API has already been acquired"
      );
    }
    acquired = true;
    return Object.freeze({
      postMessage: function (message, transfer) {
        console.assert(!transfer);
        // console.log(`do-post-message: ${JSON.stringify(message)}`);
        eclipse_receiveMessage(JSON.stringify(message));
        //   ${viewToHost.inject("JSON.stringify({what: 'postMessage', value: message})")}
      },
      setState: function (newState) {
        state = newState;
        // TODO: Route this to wherever VSCode sinks do-update-state.
        // doPostMessage('do-update-state', JSON.stringify(newState));
        // console.log(`do-update-state: ${JSON.stringify(newState)}`);
        return newState;
      },
      getState: function () {
        return state;
      },
    });
  };
})();
delete window.parent;
// delete window.top;
delete window.frameElement;

document.addEventListener("DOMContentLoaded", () => {
  console.log("DOMContentLoaded"); // TODO: update CSS styles.
  //   ${viewToHost.inject("JSON.stringify({what:'DOMContentLoaded'})")}
});
