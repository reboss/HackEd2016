
var socket = io.connect('http://localhost:3000');

$('#send').on('click', function(event) {
    event.preventDefault();
    socket.emit('data', {
	routeFrom: $('#rf').val(),
	routeTo: $('#rt').val(),
	leaving: $('#l').val()
    });
});

socket.on('callback', function(data) {
    console.log(data.done);
    // Print the data.data somewhere...
});
