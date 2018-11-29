
function ssss(par){
    return par + 1;
}

function ssds(par){
    return 2 + ssss(par);
}

var test = ssds(1) == 4