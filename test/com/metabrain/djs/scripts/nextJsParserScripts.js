var var2 = {
    par: 1,
    met: function(){
        return this.par;
    }
};
var ss = var2.met();

class Animal{
    constructor(){}
    function run(){return 1;}
}
var var2 = new Animal();
var var3 = var2.run();

function ss(par){
    for (var i=0;i<2;i++)
        par += 1;
    return par;
}

function ss(par){
    return par--;
}


var a = -1;