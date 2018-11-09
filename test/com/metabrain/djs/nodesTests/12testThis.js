var obj = {
    param: 1 + 1,
    func: function (){
        return this.param + 1;
    }
}


var test = obj.func() == 3