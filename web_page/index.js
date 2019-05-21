$(document).ready(() => {
    console.log("Document is ready!");
    
    $("#updateBtn").click(() => {
        updateTable();
    });

    updateTable();
});

function updateTable() {
    const table = $("#roomsTable");
    const tableBody = table.find('tbody');
    const address = 'http://127.0.0.1:6001/num_persons';

    // clear the table content before updating
    tableBody.empty();

    $.get(address, function(data){
        const room = data;
        // for now only one room is supported so the ID is always going to be 1 here...
        const tableRow = `<tr><th scope="row">${1}</th><td>${room.room}</td><td>${room.num_persons}</td></tr>'`
        tableBody.append($(tableRow));
    });
}