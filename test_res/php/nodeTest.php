<?php
include "db.php";

use PHPUnit\Framework\TestCase;

class nodeTest extends TestCase
{





/*
    function testClassConstructor()
    {
        $response = response_file("node.php", array(
            "node_local" => "testClassConstructor",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "level" => "15",
            "data" => "
                        class Animal {
                          constructor(name) {
                            this.name = name;
                          }
                        }

                        var ss = new Animal('cat')
                        "
        ));
        $this->assertEquals(1, 1);
    }
    */
/*
    function testObjectMethod()
    {
        $response = response_file("node.php", array(
            "node_local" => "testObjectMethod",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                        function Animal(name) {
                            this.name = name;
                            function say(){
                                if (this.name == 'cat')
                                    return 'meow'
                                return 'wow'
                            }
                        }

                        var ssds = new Animal('cat')
                        ssds.say();
                        "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][1]];
        $result_node = $nodes[$result_node["local"][0]];
        $this->assertEquals($result_node["value"], "ёжик");
    }*/

/*    function testNew2()
    {
        $response = response_file("node.php", array(
            "node_local" => "testNew2",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                        function Animal(name) {
                            this.name = name;
                            function say(){
                                if (this.name == 'cat')
                                    return 'meow'
                                return 'wow'
                            }
                        }

                        function sayAnimal(first){
                            first.say();
                        }
                        
                        sayAnimal(new Animal('cat'))
                        "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][1]];
        $result_node = $nodes[$result_node["local"][0]];
        $this->assertEquals($result_node["value"], "ёжик");
    }*/


    /*
            function testStringAddToSelf()
            {
                $response = response_file("node.php", array(
                    "node_local" => "testAddToSelf",
                    "node_path" => "php/tests",
                    "node_type" => "js_code",
                    "execute" => "1",
                    "level" => "15",
                    "data" => "
                        var str = 'h';
                        str += 'e';
                        str += 'l' + 'l';
                        str += 'o';
                        "
                ));
                $nodes = $response["nodes"];
                $node = $nodes[$response["node_id"]];
                $result_node_id = $nodes[$node["next"][0]];
                $this->assertEquals($nodes[$result_node_id]["value"], "hello");
            }

            function testNumberAddAndSubToSelf()
            {
                $response = response_file("node.php", array(
                    "node_local" => "testNumberAddAndSubToSelf",
                    "node_path" => "php/tests",
                    "node_type" => "js_code",
                    "execute" => "1",
                    "level" => "15",
                    "data" => "
                        var num = 0;
                        num += 1;
                        num += -2;
                        num -= 2;
                        num -= -2;
                        "
                ));
                $nodes = $response["nodes"];
                $node = $nodes[$response["node_id"]];
                $result_node_id = $nodes[$node["next"][0]];
                $this->assertEquals($nodes[$nodes[$result_node_id]["value"]]["data"], "-1");
            }

            function testRequire()
            {
                $response = response_file("node.php", array(
                    "node_local" => "testRequire",
                    "node_path" => "php/tests",
                    "node_type" => "js_code",
                    "execute" => "1",
                    "level" => "15",
                    "data" => "
                        var file = require('dir/dir/file');

                        var file2 = require('dir/dir/file');
                        file2 = 'hello';
                        "
                ));
                $nodes = $response["nodes"];
                $node = $nodes[$response["node_id"]];
                $result_node_id = $nodes[$node["next"][0]];
                $this->assertEquals($nodes[$nodes[$result_node_id]["value"]]["data"], "hello");
            }

            function testRequire2()
            {
                $response = response_file("node.php", array(
                    "node_local" => "testRequire",
                    "node_path" => "php/tests",
                    "node_type" => "js_code",
                    "execute" => "1",
                    "level" => "15",
                    "data" => "
                        var file = require('dir/file');

                        file = function (par){
                            return par + 1;
                        }

                        var ssds = require('dir/file')(2);
                        "
                ));
                $nodes = $response["nodes"];
                $node = $nodes[$response["node_id"]];
                $result_node_id = $nodes[$node["next"][2]];
                $this->assertEquals($nodes[$nodes[$result_node_id]["value"]]["data"], "3");
            }

            function testModuleLoad()
            {
                $response = response_file("node.php", array(
                    "node_local" => "testRequire",
                    "node_path" => "php/tests",
                    "node_type" => "js_code",
                    "execute" => "1",
                    "level" => "15",
                    "data" => "
                        var sum_to_million = require('/php/modules/sum_to_million.php');

                        var ssds = sum_to_million.sum()
                        "
                ));
                $nodes = $response["nodes"];
                $node = $nodes[$response["node_id"]];
                $result_node_id = $nodes[$node["next"][2]];
                $this->assertEquals($nodes[$nodes[$result_node_id]["value"]]["data"], "3");
            }

            function testTypeOf()
            {
                $response = response_file("node.php", array(
                    "node_local" => "testTypeOf",
                    "node_path" => "php/tests",
                    "node_type" => "js_code",
                    "execute" => "1",
                    "level" => "15",
                    "data" => "
                        var ssds = type of 'hello' //node_type to lowercase
                    "
                ));
                $nodes = $response["nodes"];
                $node = $nodes[$response["node_id"]];
                $result_node_id = $nodes[$node["next"][0]];
                $this->assertEquals($nodes[$nodes[$result_node_id]["value"]]["data"], "String");
            }

            function testForIn()
            {
                $response = response_file("node.php", array(
                    "node_local" => "testForIn",
                    "node_path" => "php/tests",
                    "node_type" => "js_code",
                    "execute" => "1",
                    "level" => "15",
                    "data" => "
                        var array = [];

                        var object = {
                            first: 1,
                            second: 2
                        };

                        for (var key in object)
                            array.push(key);
                        "
                ));
                $nodes = $response["nodes"];
                $node = $nodes[$response["node_id"]];
                $result_node_id = $nodes[$node["next"][0]];
                $this->assertEquals(count($nodes[$result_node_id]["cell"]), 2);
                $cell1_node_id = $nodes[$result_node_id]["cell"][0];
                $cell2_node_id = $nodes[$result_node_id]["cell"][0];
                $this->assertEquals($nodes[$cell1_node_id]["data"], "first");
                $this->assertEquals($nodes[$cell2_node_id]["data"], "second");
            }


            function testSwitchCaseDefault()
            {

            }

            function testThrowTryCatchFinally()
            {

            }

            function testConst()
            {

            }*/


    /*
    function testRequere(){
        $response = response_file("node.php", array(
            "node_path" => "php/tests/testRequere",
            "node_local" => "testIncrement",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                var wordBase = {};

                function getIndex(wordStr){
                    var wordChars = wordStr.split('');
                    var wordIndexPath = '';
                    for (var i=0; i<wordChars.length; i++)
                        wordIndexPath += wordChars[i] + '/';
                    return wordBase.requare(wordIndexPath);
                }

                var index = getIndex('hello');
                var result = index.node_data();
                "
        ));
        echo json_encode_readable($response);
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["value"];
        $this->assertEquals($nodes[$result_node_id]["data"], "0");
    }*/


    /*
        // TODO create new construction with inc and exit
        function testIncrementPrefix(){
            $response = response_file("node.php", array(
                "node_path" => "php/tests",
                "node_local" => "testIncrement",
                "node_type" => "js_code",
                "execute" => "1",
                "level" => "15",
                "data" => "
                function ssds(){
                    var a = 0;
                    return ++a;
                }

                ssds();
                "
            ));
            echo json_encode_readable($response);
            $nodes = $response["nodes"];
            $node = $nodes[$response["node_id"]];
            $result_node_id = $nodes[$node["next"][0]]["value"];
            $this->assertEquals($nodes[$result_node_id]["data"], "0");
        }*/


}

