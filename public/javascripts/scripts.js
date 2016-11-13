$(document).ready(function(){
    // Initialize Firebase
    var config = {
        apiKey: "<API_KEY>",
        authDomain: "bookstore-eduardo.firebaseapp.com",
        storageBucket: "gs://bookstore-eduardo.appspot.com",
    };
    firebase.initializeApp(config);

    $('#mainTable').DataTable({
        "dom": '<"row" <"col-md-4"<"pull-left"f>><"col-md-3"<"pull-right"i>>><"row"<"col-md-12" tlp>>',
        "ajax": {
            "url": jsRoutes.controllers.Application.listBooks().url,
            "dataSrc": "",
        },
        "columns": [
            {"data": "id", "width": "10%"},
            {"data": "title"},
            {"data": "author"},
            {"data": "pages", "width": "10%",},
            {
                "sortable": false,
                "width": "10%",
                "render": function (data, type, row) {
                    return '<a class="editBookBtn btn btn-lg ui-tooltip fa fa-pencil" style="font-size: 22px;" onclick="editItem(\'' + row.id + '\')" data-original-title="Edit"></a>  <a class="btn btn-lg ui-tooltip fa fa-trash-o" onclick="removeItem(\'' + row.id + '\')" style="font-size: 22px;" data-original-title="Delete"></a>';
                }
            }
        ]
    });
});
$(function() {
    $("#addBookBtn").click(function(){
        $("#modalFields").empty();
        $("#bookModal").modal("show")
        $.get(jsRoutes.controllers.Application.upsertBook(true, 0).url, function(data){
           $("#modalFields").append(data);
           $("#modalTitle").html("Create book");
        });
        configDropzone();
    });
});

function editItem(id){
   $("#modalFields").empty();
   $("#bookModal").modal("show")
   $.get(jsRoutes.controllers.Application.upsertBook(false, id).url , function(data){
       $("#modalFields").append(data);
   });
   $("#modalTitle").html("Edit book");
   configDropzone();
};

function removeItem(id){
    bootbox.confirm({
        size: "small",
        message: "Are you sure?",
        callback: function(res) {
            if(res){
                $.ajax({
                    url: jsRoutes.controllers.Application.runDeleteBook(id).url,
                    type: 'DELETE',
                    success: function(result) {
                       if (result.redirect) {
                            window.location.href = result.redirect;
                       }
                    }
                });
            }
        }
    });
};

function configDropzone() {
    Dropzone.options.fileupload = {
        paramName: "cover", // The name that will be used to transfer the file
        maxFilesize: 5, // MB
        uploadMultiple: false,
        parallelUploads: false,
        clickable: true,

        accept: function(file, done) {

        }
    };
}