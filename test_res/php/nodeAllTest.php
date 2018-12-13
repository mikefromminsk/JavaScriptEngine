<?php
include "db.php";

use PHPUnit\Framework\TestCase;

class nodeAllTest extends TestCase
{

    function testNodePath()
    {
        $response1 = response_file("node.php", array(
            "node_path" => "php/tests",
        ));
        $response2 = response_file("node.php", array(
            "node_path" => "php",
        ));
        $node2 = $response2["nodes"][$response2["node_id"]];
        $this->assertContains($response1["node_id"], $node2["dir"]);
    }

    function testNodeLocal()
    {
        $response1 = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testNodeLocal",
        ));
        $response2 = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testNodeLocal",
        ));
        $this->assertEquals($response1["node_id"], $response2["node_id"]);
    }

    function testSimpleFunction()
    {
        $response = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testSimpleFunction",
            "node_type" => "js_code",
            "level" => "15",
            "data" => "
                function ssds(){

                }

                ssds();
                "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $this->assertEquals($nodes[$node["local"][0]]["title"], "ssds");
        $this->assertEquals($nodes[$node["next"][0]]["source"], $node["local"][0]);
    }

    function testFunctionReturn()
    {
        $response = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testFunctionReturn",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                    function ssds(){
                        return 3;
                    }

                    ssds();
                    "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["value"];
        $this->assertEquals($nodes[$result_node_id]["data"], "3");
    }

    function testFunctionReturnExpression()
    {
        $response = response_file("node.php", array(
            "node_local" => "testFunctionReturnExpression",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
            function ssds(){
                return 2 + 2 * 2;
            }
            
            ssds();
            "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["value"];
        $this->assertEquals($nodes[$result_node_id]["data"], "6");
    }


    function testFunctionVariables()
    {
        $response = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testFunctionVariables",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
            function ssds(){
                var a = 1;
                return 2 + a;
            }
            
            ssds();
            "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["value"];
        $this->assertEquals($nodes[$result_node_id]["data"], "3");
    }

    function testFunctionParams()
    {
        $response = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testFunctionParams",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                function ssds(par){
                    return par;
                }
                ssds(1);
                ssds();
                "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][0]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][1]];
        $this->assertEquals($result_node["value"], null);
    }

    function testSeveralFunctions()
    {
        $response = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testSeveralFunctions",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                   function ssss(par){
                       return par + 1;
                   }

                   function ssds(par){
                       return 2 + ssss(par);
                   }

                   ssds(1);
                   "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["value"];
        $this->assertEquals($nodes[$result_node_id]["data"], "3");
    }


    function testIncrementPostfix()
    {
        $response = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testIncrement",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                       function ssds(){
                           var a = 0;
                           return a++;
                       }

                       ssds();
                       "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["value"];
        $this->assertEquals($nodes[$result_node_id]["data"], "1");
    }

    function testFor()
    {
        $response = response_file("node.php", array(
            "node_path" => "php/tests",
            "node_local" => "testFor",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
            function ssds(){
                var a = 0;
                for (var i=0; i<10; i++)
                    a = a + 1;
                return a;
            }
            
            ssds();
            "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["value"];
        $this->assertEquals($nodes[$result_node_id]["data"], "10");
    }

    function testPushToArray()
    {
        $response = response_file("node.php", array(
            "node_local" => "testPushToArray",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                var array = [];
                array.push('hello');
                "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]];
        $result_node_id = $nodes[$result_node_id["source"]];
        $result_node_id = $nodes[$result_node_id["value"]];
        $this->assertEquals($result_node_id["cell"], "hello");
    }

    function testStringReverse()
    {
        $response = response_file("node.php", array(
            "node_local" => "testStringReverse",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                var node = 'hello';
                var rev = node.reverse();
                "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node_id = $nodes[$node["next"][0]]["source"];
        $hello_str = $nodes[$result_node_id]["value"];
        $this->assertEquals($hello_str, "hello");

        $result_node_id = $nodes[$node["next"][1]]["source"];
        $hello_str = $nodes[$result_node_id]["value"];
        $this->assertEquals($hello_str, "olleh");
    }


    function testCloneObject()
    {
        $response = response_file("node.php", array(
            "node_local" => "testCloneObject",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                var a = {b: 1, c: 2};
                var b = a.b;
                var c = a.c;
            "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][1]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][2]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "2");
    }

    function testObjectCloneWithMethod()
    {
        $response = response_file("node.php", array(
            "node_local" => "testObjectCloneWithMethod",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                var obj = {
                    param: 1 + 1,
                    func: function (){
                        return 1 + 2;
                    }
                 }
                var res = obj.func();
             "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node = $nodes[$node["local"][1]];
        $this->assertEquals($nodes[$result_node["value"]]["data"], "3");
    }


    function testThis()
    {
        $response = response_file("node.php", array(
            "node_local" => "testThis",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                var obj = {
                    param: 1 + 1,
                    func: function (){
                        return this.param + 1;
                    }
                 }
                obj.func();
             "
        ));
        $nodes = $response["nodes"];
        $node = $nodes[$response["node_id"]];
        $result_node = $nodes[$node["next"][1]];
        $this->assertEquals($nodes[$result_node["value"]]["data"], "3");
    }


    function testSetPrototype()
    {
        $response = response_file("node.php", array(
            "node_local" => "testSetPrototype",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                array = [];
            
                array.prototype.return_1 = function (str){
                    return 1;
                }
                
                array.return_1();
                "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][2]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
    }

    // var node = 'hello'.reverse(); Запрещено


    function testReturnInSecondLevel()
    {
        $response = response_file("node.php", array(
            "node_local" => "testReturnInSecondLevel",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                function ssds(par){
                    if (par == 1)
                        return 1;
                    return 2;
                }
                
                ssds(1);
                ssds(2);
                
                "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][0]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][1]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "2");
    }


    function testThisStringPrototype()
    {
        $response = response_file("node.php", array(
            "node_local" => "testThisStringPrototype",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                
                str = 'str'
                str.prototype.str_reverse = function (){
                    return this.reverse();
                }
                
                str.str_reverse();
            
                "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][2]];
        $this->assertEquals($result_node["value"], "rts");
    }


    function testLocalFindVariables()
    {
        $response = response_file("node.php", array(
            "node_local" => "testLocalFindVariables",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
            var param = 2;

            var obj = {
                param: 1,
                func: function (){
                    return param
                }
            }

            obj.func();
            "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][2]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
    }

    function testGlobalFindVariables()
    {
        $response = response_file("node.php", array(
            "node_local" => "testGlobalFindVariables",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
            var param1 = 2;

            var obj = {
                param: 1,
                func: function (){
                    return param1
                }
            }

            obj.func();
            "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][2]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "2");
    }


    function testArrayPrototypeFromConst()
    {
        $response = response_file("node.php", array(
            "node_local" => "testArrayPrototypeFromConst",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                        var array = [];

                        array.prototype.push_reverse = function (str){
                            this.push(str.reverse())
                        }

                        array.push_reverse('hello');
                        "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][0]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["cell"], "olleh");
    }
    function testArrayPrototype()
    {
        $response = response_file("node.php", array(
            "node_local" => "testArrayPrototype2",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                array = [];
            
                array.prototype.push_reverse = function (){
                    this.push(1);
                }
                
                array.push_reverse();
                "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][0]];
        $result_node = $nodes[$result_node["value"]];
        $result_node = $nodes[$result_node["cell"][0]];
        $this->assertEquals($result_node["data"], "1");
    }

    function testSuperClassPrototype()
    {
        $response = response_file("node.php", array(
            "node_local" => "testSuperClassPrototype",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                        var animal = {
                            eats: false
                        };
                        var rabbit = {
                            jumps: true
                        };

                        rabbit.prototype = animal;

                        var a = rabbit.eats;
                        var b = rabbit.jumps;
                        "
        ));
        $nodes = $response["nodes"];;
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][2]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "0");
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][3]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
    }


    function testCaseSensitive()
    {
        $response = response_file("node.php", array(
            "node_local" => "testCaseSensitive",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                            var animal = 1;
                            var Animal = 2;
                            "
        ));

        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][0]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");

        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][1]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "2");
    }

    function testClassMethod()
    {
        $response = response_file("node.php", array(
            "node_local" => "testClassMethod",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                    class Animal {
                      speak(){
                        return 1;
                      }
                    }
                    
                    var ss = new Animal();
                    ss.speak();
                    "
        ));
        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["next"][1]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
    }


    function testClassLikeFunction()
    {
        $response = response_file("node.php", array(
            "node_local" => "testClassLikeFunction",
            "node_path" => "php/tests",
            "node_type" => "js_code",
            "execute" => "1",
            "level" => "15",
            "data" => "
                        function Animal(names) {
                            this.name = names;
                            this.canWalk = true;
                        }

                        var animal = new Animal('ёжик');
                        animal.name;
                        "
        ));

        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][1]];
        $result_node = $nodes[$result_node["local"][0]];
        $this->assertEquals($result_node["value"], "ёжик");

        $nodes = $response["nodes"];
        $result_node = $nodes[$response["node_id"]];
        $result_node = $nodes[$result_node["local"][1]];
        $result_node = $nodes[$result_node["local"][1]];
        $result_node = $nodes[$result_node["value"]];
        $this->assertEquals($result_node["data"], "1");
    }

}
