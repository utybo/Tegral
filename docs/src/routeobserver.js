import ExecutionEnvironment from "@docusaurus/ExecutionEnvironment";
import * as ackeeTracker from "ackee-tracker";

let tracker = ackeeTracker.create("https://ackee.blastoise-1.zoroark.guru", {
  detailed: true,
  ignoreLocalhost: true,
  ignoreOwnVisits: true,
});

let siteId = "cea4d752-ff51-4b02-a9c4-f29765562938";
let stop = null;

export function onRouteDidUpdate({ location, previousLocation }) {
  if (stop != null) {
    stop();
  }
  console.log(location);
  if (
    previousLocation == null ||
    location.pathname !== previousLocation.pathname
  ) {
    stop = tracker.record(siteId).stop;
  }
}
