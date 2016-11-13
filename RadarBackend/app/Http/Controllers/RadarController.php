<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\Request;
use Illuminate\Support\Facades\Input;
use Psy\Util\Json;
use App\Http\Requests;
use Workerman\Worker;
use PHPSocketIO\SocketIO;


class RadarController extends Controller
{
    public function __construct() {
        $this->middleware('guest');
    }

    public function broadcast($json) {
        $io = new SocketIO(5555);
        $io->on('get fence', function($socket) use($json) {
            $socket->broadcast->emit('broadcast', $json);
        });

        Worker::runAll();
        return view('data', compact('json'));

    }

    public function receive() {
        return "You gave me: stuff";
    }

}
