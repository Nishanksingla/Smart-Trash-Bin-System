app.controller("aptController", function ($scope, $http, $sce, client, esFactory) {
    $scope.devices = [];

    $scope.series = [
        { name: "level", data: [] }
    ];

    // $scope
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
            $scope.devices.push(values._id);
        });
    }, function (error) {
        console.trace(error.message);
    });


    $scope.updateGraph = function () {
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
                                                "gte": "now-90s",
                                                "lte": "now"
                                                // "format": "epoch_millis"
                                            }
                                        }
                                    },
                                    {
                                        "match": {
                                            "id": parseInt($scope.deviceNum)
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
                    "incoming": {
                        "date_histogram": {
                            "field": "@timestamp",
                            "interval": "10s",
                            "time_zone": "America/Los_Angeles",
                            "min_doc_count": 1
                        },
                        "aggs": {
                            "dist": {
                                "avg": {
                                    "field": "distance"
                                }
                            }
                        }
                    }
                }
            }
        }).then(function (resp) {
            var dataES = resp.aggregations.incoming.buckets;
            angular.forEach(dataES, function (values) {
                $scope.charts.series[0].addPoint([values.key, values.dist.value]);
            });
            setTimeout($scope.updateGraph, 5000);
        }, function (error) {
            console.trace(error.message);
        });
    }

    $scope.changeGraph = function () {
        debugger
        if ($scope.deviceNum) {
            debugger
       
        client.search({
            index: 'data',
            body: {
                "query": {
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
                                    },
                                    {
                                        "match": {
                                            "id": parseInt($scope.deviceNum)
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
                            "time_zone":
                            "America/Los_Angeles",
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
            //---------HIGH CHARTS----------------------------------------------------------------------------------------------------        
            $scope.charts = new Highcharts.chart('container2', {
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

        }, function (error) {
            alert(error);
        })
    }
    }

});
