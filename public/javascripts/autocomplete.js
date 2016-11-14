var baseUrlReq = "https://www.googleapis.com/books/v1/volumes?q=";
var res = [];
function buildResults(items) {
    var length = items.length;
    for(i = 0; i < length; i++) {
        item = items[i];
        if(item.volumeInfo.authors && item.volumeInfo.title) {
            res[item.volumeInfo.title] = {
                author: item.volumeInfo.authors.join("; "),
                pagecount: item.volumeInfo.pageCount,
                description: item.volumeInfo.description
            }
        }
    }
    return Object.keys(res);

}

function executeTitleSearch(process) {

    var searchObj = {
        "intitle": $("#title").val()
    }
    var q = $("#title").val();
    if(q) {
        var search = baseUrlReq + q;
        for (var key in searchObj) {
            if (searchObj.hasOwnProperty(key) && searchObj[key]) {
                search += "+" + key + ":" + searchObj[key] + "+";
            }
        }
        $.get(search, function(resp){
            if(resp.totalItems > 0) {
               var results = buildResults(resp.items);
               process(results);
            }
        });
    }

}

function autoFill(item) {
    var author = res[item].author;
    var pagecount = res[item].pagecount;
    var description = res[item].description;

    if(author.length <= $("#author").attr("maxlength")) {
        $("#author").val(author);
    }
    if(pagecount && pagecount <= $("#pages").attr("max")) {
        $("#pages").val(pagecount);
    }
    if(description && description.length <= $("#description").attr("maxlength")) {
        $("#description").val(description);
    }
    res = []; //Freeing up memory
}