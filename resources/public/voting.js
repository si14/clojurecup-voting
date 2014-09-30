$.getJSON("favs.dump.json", function(data) {
    var datasets = [];

    var teamNames = []
    $.each(data.teams, function(key, val) {
        teamNames.push(key);
    });
    teamNames.sort();

    var maxValues = [];
    for (var i = 0; i < teamNames.length; i++) {
        var votes = data.teams[teamNames[i]];
        maxValues.push(votes[votes.length - 1]);
    }

    maxValues.sort(function(a, b) {return a - b});
    quantile = maxValues[Math.floor((maxValues.length - 1) * 0.9)];

    $.each(data.timestamps, function(_, timestamp) {
        datasets.push([new Date(timestamp)]);
    });

    var labels = ['time'];
    $.each(teamNames, function(_, teamName) {
        var votes = data.teams[teamName];

        if (votes[votes.length - 1] < quantile) {
            return;
        };

        labels.push(teamName);

        $.each(data.teams[teamName], function(i, votes) {
            datasets[i].push(votes);
        });
    });

    new Dygraph(document.getElementById("plot"),
                datasets,
                {
                    labels: labels,
                    drawGrid: false,
                    stepPlot: true,
                    strokeWidth: 1,
                    legend: "always",
                    labelsDiv: "legend",
                    labelsSeparateLines: true,
                    width: 900,
                    height: 400
                }
               );
});
