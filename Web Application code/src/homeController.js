app.controller("homeController", function ($scope, $http, $sce, client, esFactory) {
    $scope.prevDist = 0;
    $scope.series = [
        { name: "level", data: [] }
    ];

    // $scope.currentNumBags = 8;

    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    $scope.threshold = 24;

    client.search({
        index: 'devices',
        body: {
            "query": {
                "match_all": {

                }
            }
        }
    }).then(function (resp) {
        angular.forEach(resp.hits.hits, function (values) {
            if (values._id === "1") {
                $scope.currentNumBags = values._source.numBags;
            }
        });
    }, function (error) {
        console.trace(error.message);
    });



    $scope.updateGraph = function () {
        client.search({
            index: 'data',
            type: "record",
            body: {
                "query": {
                    "match_all": {}
                },
                "size": 1,
                "sort": [
                    {
                        "@timestamp": {
                            "order": "desc"
                        }
                    }
                ]
            }
        }).then(function (resp) {
            //debugger
            // resp.hits.hits[0]._source.distance
            var y = resp.hits.hits[0]._source.distance;
            var x = resp.hits.hits[0].sort[0];

            if ($scope.prevDist !== y) {
                $scope.prevDist = y;
                $scope.charts.series[0].addPoint([x, y]);
                if (y === $scope.threshold) {
                    $scope.currentNumBags = $scope.currentNumBags - 1;
                    client.update({
                        index: 'devices',
                        type: 'rec',
                        id: '1',
                        body: {
                            doc: {
                                numBags: $scope.currentNumBags
                            }
                        }
                    }, function (error, response) {
                        alert(error);
                    });
                }
            }
            setTimeout($scope.updateGraph, 5 * 1000);
        }, function (error) {
            console.trace(error.message);
        });
    }

    $scope.Graph = function () {
        client.search({
            index: 'data',
            body: {
                query: {
                    "filtered": {
                        "query": {
                            "query_string": {
                                "analyze_wildcard": true,
                                "query": "*"
                            }
                        },
                        "filter": {
                            "bool": {
                                "must": [
                                    {
                                        "range": {
                                            "@timestamp": {
                                                "gte": "now-15m",
                                                "lte": "now"
                                                // "format": "epoch_millis"
                                            }
                                        }
                                    }
                                ],
                                "must_not": []
                            }
                        }
                    }
                },
                "size": 0,
                "aggs": {
                    "data": {
                        "date_histogram": {
                            "field": "@timestamp",
                            "interval": "10s",
                            "time_zone": "America/Los_Angeles",
                            "min_doc_count": 1
                        },
                        "aggs": {
                            "distance": {
                                "avg": {
                                    "field": "distance"
                                }
                            }
                        }
                    }
                }
            }
        }).then(function (resp) {
            console.log(resp);

            var dataES = resp.aggregations.data.buckets;

            angular.forEach(dataES, function (values) {
                $scope.series[0].data.push({ x: values.key, y: values.distance.value });
            });
            if (dataES.length > 0) {
                $scope.prevDist = dataES[dataES.length - 1].distance;
            }

            //debugger
            //---------HIGH CHARTS----------------------------------------------------------------------------------------------------        
            $scope.charts = new Highcharts.chart('container', {
                chart: {
                    type: 'column',
                    events: {
                        load: $scope.updateGraph
                    }
                },
                title: {
                    text: 'Smart Trashbin'
                },
                xAxis: {
                    type: 'datetime',
                    tickPixelInterval: 50,
                },
                yAxis: {
                    title: {
                        text: 'Level'
                    }
                },
                series: $scope.series
            });
            // ------------------------------------------------------------------------------------------------------------------------
        }, function (error) {
            console.trace(error.message);
        });
    }

    $scope.setGarbageBags = function () {
        client.update({
            index: 'devices',
            type: 'rec',
            id: '1',
            body: {
                doc: {
                    numBags: $scope.numGarbageBags
                }
            }
        }, function (error, response) {
            //debugger
            $scope.currentNumBags = $scope.numGarbageBags;
        });
    }
    
    $scope.Graph();

});