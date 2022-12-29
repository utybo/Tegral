import ExecutionEnvironment from "@docusaurus/ExecutionEnvironment";
import * as ackeeTracker from "ackee-tracker";

const module = {};

if (ExecutionEnvironment.canUseDOM) {
  let tracker = ackeeTracker.create("https://ackee.blastoise-1.zoroark.guru", {
    detailed: true,
    ignoreLocalhost: true,
    ignoreOwnVisits: true,
  });

  let siteId = "cea4d752-ff51-4b02-a9c4-f29765562938";
  let stop = null;

  module.onRouteDidUpdate = ({ location, previousLocation }) => {
    if (location != undefined) {
      if (stop != null) {
        stop();
      }
      if (
        previousLocation == null ||
        location.pathname !== previousLocation.pathname
      ) {
        stop = tracker.record(siteId).stop;
      }
    }
  };
}
export default module;
