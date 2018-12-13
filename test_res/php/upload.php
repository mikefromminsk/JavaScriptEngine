<?php

include "db.php";
error_reporting(1);

$data = get_string_requared("data");
$extension = get_string_requared("extension");

$md5_hash = md5($data);
$filename = "$md5_hash.$extension";
$filepath = "../upload/$filename";
file_put_contents($filepath, $data);

$result["link_url"] = "upload/$filename";
response($result);
