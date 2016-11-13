var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var logger = require('morgan');

app.use(logger('dev'));

app.get('/', function(req, res) {
    res.sendFile(__dirname + "/index.html");
});

io.sockets.on('connection', function(socket) {

    socket.on('new data', function(data) {
	io.sockets.emit('push data', data);
    });
});

var port = process.env.PORT || 3000;
http.listen(port, function() {
    console.log("Listening on port " + port);
});
