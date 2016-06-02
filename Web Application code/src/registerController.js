app.controller("registerController", function ($scope, $http, client, esFactory) {

    $scope.userInfo = {};
    $scope.register = function () {
        debugger
        console.log($scope.userInfo);
        client.index({
            index: 'registration',
            type: 'account',
            body: $scope.userInfo
        }, function (error, response) {
            debugger
            console.log(response);
            if (response.created) {
                window.location.assign('/');
            }
        });
    }
});


            // {
            //     "query": {
            //         "filtered": {
            //             "query": {
            //                 "query_string": {
            //                     "analyze_wildcard": true,
            //                     "query": "*"
            //                 }
            //             },
            //             "filter": {
            //                 "bool": {
            //                     "must": [
            //                         {
            //                             "range": {
            //                                 "@timestamp": {
            //                                     "gte": "now-90s",
            //                                     "lte":"now"
            //                                     // "format": "epoch_millis"
            //                                 }
            //                             }
            //                         }
            //                     ],
            //                     "must_not": []
            //                 }
            //             }
            //         }
            //     },
            //     "size": 0,
            //     "aggs": {
            //         "incoming": {
            //             "date_histogram": {
            //                 "field": "@timestamp",
            //                 "interval": "10s",
            //                 "time_zone":
            //                 "America/Los_Angeles",
            //                 "min_doc_count": 1
            //             },
            //             "aggs": {
            //                 "dist": {
            //                     "avg": {
            //                         "field": "distance"
            //                     }
            //                 }
            //             }
            //         }
            //     }
            // }
                        // var dataES = resp.aggregations.incoming.buckets;
            // angular.forEach(dataES, function (values) {
            //     $scope.charts.series[0].addPoint([values.key, values.dist.value]);
            //     if (values.dist.value === $scope.threshold) {
            //         $scope.currentNumBags = $scope.currentNumBags - 1;

            //         client.update({
            //             index: 'devices',
            //             type: 'rec',
            //             id: '1',
            //             body: {
            //                 doc: {
            //                     numBags: $scope.currentNumBags
            //                 }
            //             }
            //         }, function (error, response) {
            //             debugger
            //             // $scope.currentNumBags = $scope.numGarbageBags;
            //         });
            //     }
            // });
