<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

use App\Http\Requests;

class RadarController extends Controller
{
    public function receive(Request $request) {
        dd($request);
        $data = $request->all();
        return reponse()->json($data);
    }

}
