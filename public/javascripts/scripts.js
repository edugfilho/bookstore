$(document).ready(function(){
    $("#success-alert").hide();
    //Init datatable
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
            {   //Thumbnail
                "sortable": false,
                "width": "10%",
                "render": function (data, type, row) {
                    var image = new Image();
                    image.src = "data:image/jpeg;base64,";
                    image.src += row.thumbnail;
                    return '<img src="'+ image.src + '" class="img-thumbnail" data-toggle="tooltip" title="Click to see original size" alt="Book cover" onclick="showImg('+ row.coverId +')"/>';
                }
            },
            {   //Actions "Edit" and "Delete"
                "sortable": false,
                "width": "10%",
                "render": function (data, type, row) {
                    return '<a class="editBookBtn btn btn-lg ui-tooltip glyphicon glyphicon-pencil" onclick="editItem(\'' + row.id + '\')" data-original-title="Edit"></a>  <a class="btn btn-lg ui-tooltip glyphicon glyphicon-trash" onclick="removeItem(\'' + row.id + '\')" data-original-title="Delete"></a>';
                }
            }
        ]
    });
    configDropzone();

});

function showImg(coverId) {
    var imgUrl = jsRoutes.controllers.Application.fileDownload(coverId).url;
    bootbox.alert('<img src="'+ imgUrl + '" class="img-responsive" alt="Book cover" />')

}

function cleanModal() {
    $("#modalFields").empty();
    $("#coverPicture").remove();
}

$(function() {
    $("#addBookBtn").click(function(){
        cleanModal();
        resetDropArea();
        $("#bookModal").modal("show")
        $.get(jsRoutes.controllers.Application.upsertBook(true, 0).url, function(data){
           $("#modalFields").append(data);
           $("#modalTitle").html("Create book");
           $('.typeahead').typeahead({
                minLength: 3,
                source: function (query, process) {
                    return executeTitleSearch(process);
                },
                afterSelect: function (item) {
                    autoFill(item);
                }
           });
        });
    });

});

$(function() {
    $("#submitBook").click(function(){
        $bookForm = $('#bookForm')[0];
        if(!$bookForm.checkValidity()){ // Form not valid? show UI errors.
            $('<input type="submit">').hide().appendTo($bookForm).click().remove();
        } else {
            var postUrl = jsRoutes.controllers.Application.runUpsertBook().url;
            $.post(postUrl, $('#bookForm').serialize())
            .done(function(result){
                if (result.redirect) {
                    window.location.href = result.redirect;
                }
            })
            .fail(function(xhr, status, error) {
                var jsonErr = xhr.responseJSON;
                $("#fileupload").html('<div class="alert alert-danger"> <strong>This book needs a cover!</strong></div>');
            });
        }
    });

});

function editItem(id){
    cleanModal();
    $("#bookModal").modal("show")
    var coverId;
    $.get(jsRoutes.controllers.Application.upsertBook(false, id).url , function(data){
       $("#modalFields").append(data);
       coverId = $(data).find("#coverId").val();
        $('.typeahead').typeahead({
            minLength: 3,
            source: function (query, process) {
               return executeTitleSearch(process);
            },
            afterSelect: function (item) {
               autoFill(item);
            }
        });
       placeCover();
    });
    $("#modalTitle").html("Edit book");
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

function placeCover(memoryImg) {
    var coverId = $("#coverId").val();
    var imgUrl = memoryImg || jsRoutes.controllers.Application.fileDownload(coverId).url;
    $("#fileupload").html('<img src="'+ imgUrl + '" id="coverPicture" class="cover-picture" alt="Book cover" />')

}

function resetDropArea() {
    $("#fileupload").html('<p class="help-block text-center" stryle="top: 100px">Drag and drop your cover picture here </p>');

}

function removeCover(dropzoneObj) {
    $("#coverPicture").remove();

}

function configDropzone() {
    Dropzone.options.fileupload = {
        url: jsRoutes.controllers.Application.fileUpload().url,
        paramName: "cover", // The name that will be used to transfer the file
        maxFilesize: 5, // MB
        parallelUploads: 1,
        clickable: true,
        autoProcessQueue: true,
        addRemoveLinks: true,
        acceptedFiles: '.jpg,.jpeg,.JPEG,.JPG,.png,.PNG',
        dictDefaultMessage: "",
        init: function() {
            var coverDropzone = this;
            coverDropzone.currentUpload = null;
            $("#addBookBtn").click(function(){
                coverDropzone.removeAllFiles(true);
            });
            this.on("dragenter", function(file){
                //Can't empty content while upload is in progress. Must happen with removeFile()
                if(!coverDropzone.currentUpload){
                    $("#fileupload").children().hide();
                }
            });
            this.on("dragleave", function(file){
                if(!coverDropzone.currentUpload){
                    $("#fileupload").children().show();
                }
            });
            this.on("addedfile", function(file){
                //If a new file is dropped while an upload is in progress, abort the current upload
                if(coverDropzone.currentUpload) {
                    coverDropzone.removeFile(coverDropzone.currentUpload);
                } else {
                    removeCover();
                }
                coverDropzone.currentUpload = file;
            });
            this.on("removedfile", function(file){
                coverDropzone.currentUpload = null;
            });
            this.on("cancelled", function(file){
                coverDropzone.currentUpload = null;
            });
            this.on("error", function(file){
                coverDropzone.currentUpload = null;
            });
            this.on("success", function(file, resp) {
                $("#coverId").val(resp.id);
                placeCover();
                coverDropzone.currentUpload = null;
            });
        }
    };

}