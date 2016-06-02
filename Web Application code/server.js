var express = require('express');
var bodyParser = require('body-parser');
var app = express();
var fs = require('fs'),
    https = require('https');
app.use('/', express.static(__dirname + '/src'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.post('/login', function (req, res) {
    // console.log('login');
    // console.log("req.body" + JSON.stringify(req.body));

    if (req.body.username === "nishank" && req.body.password === "singla") {
        res.json({ message: "success" });
        console.log('password correct');
        return;
    } else {
        res.json({ message: "wrong" });
        return;
    }

})
app.all('/*', function (req, res, next) {
    res.sendFile('index.html', { root: __dirname + '/src' });
});
app.listen(80, function () {
    console.log('server has started on port 80!');
});
