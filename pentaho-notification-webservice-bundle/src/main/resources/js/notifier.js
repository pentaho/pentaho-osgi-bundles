'use-strict';

define(["common-ui/angular", "common-ui/angular-resource"], function(angular) {

  var pollerModule = angular.module('NotificationServiceModule', ['ngResource']);

  pollerModule.factory('NotificationService', [
    '$http', '$q',
    function($http, $q) {
      // {<regNumber>: {
      //    notificationType: <notifType>,
      //    callback:         <callback>,
      //    interestedIds: {<interestedId>: <latestSequence>}
      //  }
      // }
      var registrations = {};
      var registrationCount = 0;
      var nextRegNumber = 1;

      // Registrations indexed in a way that it
      // makes it direct to process poll responses.
      // Invalidated in `register` and in `unregister`.
      // Lazily created by getResponseMap.
      //
      // {<notifType>: {<interestedId>: [<registration>]}}
      var responseMap = null;

      // If we're currently polling.
      var running = false;

      var cancelPromise = $q.defer();

      function cancelPolling() {
        running = false;
        cancelPromise.resolve();
      }

      function singlePoll() {
        var requestData = buildRequestData();

        running = true;
        cancelPromise = $q.defer();

        $http({
          method: 'POST',
          url:    '/cxf/notificationService',
          data: { requests: requestData },
          headers: {'Content-Type': 'application/json'},
          timeout: cancelPromise.promise
        })
        .success(function(respData) {
          processResponseData(respData);

          // Whenever there's a registration, there's need to keep polling.
          if(registrationCount) {
            singlePoll();
          } else {
            running = false;
          }
        });
      }

      function mergeRegistrations() {
        // {<notifType>: {<interestedId>: minSequence}}
        var mergedRegistrations = {};

        for(var regNumber in registrations) {
          var registration = registrations[regNumber],
              interestedIdsMap = registration.interestedIds,
              mergedInterestedIdsMap = getLazyMap(mergedRegistrations, registration.notificationType);

          for(var interestedId in interestedIdsMap) {
            var sequence = interestedIdsMap[interestedId],
                minSequence = mergedInterestedIdsMap[interestedId];

            // For each interestedId, take the minimum sequence, overall registrations.
            mergedInterestedIdsMap[interestedId] = minSequence == null
              ? sequence : Math.min(minSequence, sequence);
          }
        }

        return mergedRegistrations;
      }

      function buildRequestData() {
        var mergedRegistrations = mergeRegistrations();

        return Object.keys(mergedRegistrations).map(function(notifType) {

          var mergedInterestedIdsMap = mergedRegistrations[notifType],
              notifTypeEntries = Object.keys(mergedInterestedIdsMap).map(function(interestedId) {
                return {
                  id:   interestedId,
                  sequence: mergedInterestedIdsMap[interestedId] // minSequence
                };
              });

          return {notificationType: notifType, entries: notifTypeEntries};
        });
      }

      function processResponseData(response) {
        if(!response || !response.notificationObjects) return;

        // Defensive copy.
        var respMap = getResponseMap();

        // May have unregistered while waiting.
        if(!respMap) return;

        response.notificationObjects.forEach(function(notificationObject) {
          var respNotifTypeMap = respMap[notificationObject.type],
              interestedId   = notificationObject.id,
              sequence       = notificationObject.sequence,
              interestedRegs = respNotifTypeMap[interestedId],
              changedObject  = notificationObject.object;

          // May have unregistered while waiting.
          if(interestedRegs) {
            interestedRegs.forEach(function(registration) {
              if(registration.interestedIds[interestedId] <= sequence) {
                registration.interestedIds[interestedId] = sequence + 1;

                registration.callback(changedObject);
              }
            });
          }
        });
      }

      function getResponseMap() {
        return responseMap || (responseMap = buildResponseMap());
      }

      function buildResponseMap() {
        // {<notifType>: {<interestedId>: [<registration>]}}
        var newResponseMap = null;

        if(registrationCount) {
          newResponseMap = {};

          for(var regNumber in registrations) {
            var registration = registrations[regNumber],
                newInterestedIdsMap = getLazyMap(newResponseMap, registration.notificationType);

            for(var interestedId in registration.interestedIds) {
              getLazyArray(newInterestedIdsMap, interestedId).push(registration);
            }
          }
        }

        return newResponseMap;
      }

      function refreshPollConfig() {
        cancelPolling();
        responseMap = null;
        if(!running && registrationCount >= 1) {
          singlePoll();
        }
      }

      function register(notifType, interestedIds, callback) {
        var interestedIdsMap = {};
        interestedIds.forEach(function(id) {
          interestedIdsMap[id] = 0; // the oldest possible sequence.
        });

        var regNumber = nextRegNumber++;
        // assert !registrations[regNumber];

        registrations[regNumber] = {
          notificationType: notifType,
          interestedIds: interestedIdsMap,
          callback: callback
        };
        registrationCount++;

        refreshPollConfig();

        return regNumber;
      }

      function unregister(regNumber) {
        if(registrations[regNumber]) {
          delete registrations[regNumber];
          registrationCount--;

          refreshPollConfig();
        }
      }

      return {
        register:   register,
        unregister: unregister
      };
    }
  ]);

  function getLazyMap(o, p) {
    return o[p] || (o[p] = {});
  }

  function getLazyArray(o, p) {
    return o[p] || (o[p] = []);
  }

  return pollerModule;
});