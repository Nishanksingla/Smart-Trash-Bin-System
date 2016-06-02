app.controller("cityController", function ($scope, $http) {
    debugger
    var myCenter = new google.maps.LatLng(37.3382, -121.8863);
    $scope.locations = [
        {
            "id": "0001",
            // "type": "RetailLocation",
            "address": "158 E San Fernando St San Jose, CA 95112",
            "latitude": 37.335920,
            "longitude": -121.885804,
            "level": 50
        },
        {
            "id": "0002",
            // "type": "RetailLocation",
            "address": "315 E San Fernando St San Jose, CA 95112",
            "latitude": 37.337348,
            "longitude": -121.882689,
            "level": 70
        },
        {
            "id": "0003",
            // "type": "RetailLocation",
            "address": "201 S 10th St San Jose, CA 95112",
            "latitude": 37.337407,
            "longitude": -121.878647,
            "level": 100
        }
    ]



    $scope.loadMap = function () {
        var mapObj = {
            center: myCenter,
            zoom: 16,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var infowindow = new google.maps.InfoWindow({
            content: "Hello World"
        });




        var map = new google.maps.Map(document.getElementById("map"), mapObj);

        angular.forEach($scope.locations, function (values, key) {
            var newCenter = new google.maps.LatLng(values.latitude, values.longitude);
            var secretMessages = ['This', 'is', 'the', 'secret', 'message'];
            var marker = new google.maps.Marker({
                position: newCenter
            });
            marker.setMap(map);
            if (values.level >= 100) {
                var circle = new google.maps.Circle({
                    map: map,
                    radius: values.level,
                    fillColor: 'red'
                });
                circle.bindTo('center', marker, 'position');
            } else {
                var circle = new google.maps.Circle({
                    map: map,
                    radius: values.level,
                    fillColor: 'green'
                });
                circle.bindTo('center', marker, 'position');
            }
            marker.addListener('click', function () {

                var data = new google.visualization.DataTable();
                data.addColumn('timeofday', 'Time of Day');
                data.addColumn('number', 'Levels');

                data.addRows([
                    [[8, 30, 45], 5],
                    [[9, 0, 0], 10],
                    [[10, 0, 0, 0], 12],
                    [[10, 45, 0, 0], 13],
                    [[11, 0, 0, 0], 15],
                    [[12, 15, 45, 0], 20],
                    [[13, 0, 0, 0], 22],
                    [[14, 30, 0, 0], 25],
                    [[15, 12, 0, 0], 30],
                    [[16, 45, 0], 32],
                    [[16, 59, 0], 42]
                ]);

                var options = google.charts.Bar.convertOptions({
                    title: 'Levels Throughout the Day',
                    height: 200
                });

                var node = document.createElement('div');


                var chart = new google.charts.Bar(node);

                chart.draw(data, options);

                infowindow.setContent(node);
                infowindow.open(marker.get('map'), marker);
            });
        });
    }

    $scope.loadMap();

})
