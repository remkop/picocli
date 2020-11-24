function addBlockSwitches() {

    var counter = 200

    $('.primary').each(function() {
        primary = $(this);
        createSwitchItem(primary, createBlockSwitch(primary), counter, true);
        primary.children('.tab-content').append(primary.children('.content').first().addClass('tab-pane').addClass('active').removeClass('content').attr('id', 'tab' + counter++).attr('role', 'tabpanel'))

        primary.children('.title').remove();
    });

    counter++

    $('.secondary').each(function(idx, node) {
        secondary = $(node);
        primary = findPrimary(secondary);
        switchItem = createSwitchItem(secondary, primary.children('.nav'), counter, false);
        switchItem.content.addClass('tab-pane').attr('id', 'tab' + counter).attr('role', 'tabpanel').removeClass('content');
        primary.children('.tab-content').append(switchItem.content)
        secondary.remove();
        counter++
    });
}

function createBlockSwitch(primary) {
    blockSwitch = $('<ul class="nav nav-pills" role="tablist"></ul>');
    primary.append(blockSwitch);
    contentSwitch = $('<div class="tab-content py-4"></div>');
    primary.append(contentSwitch);
    return blockSwitch;
}

function findPrimary(secondary) {
    candidate = secondary.prev();
    while (!candidate.is('.primary')) {
        candidate = candidate.prev();
    }
    return candidate;
}

function createSwitchItem(block, blockSwitch, counter, active) {

    blockName = block.children('.title').text();
    content = block.children('.content').first();
    if (active) {
        item = $('<li class="nav-item"><a class="nav-link active show" data-toggle="tab" href="#tab' + counter + '" role="tab">' + blockName + '</a></li>');
    }
    else {
        item = $('<li class="nav-item"><a class="nav-link" data-toggle="tab" href="#tab' + counter + '" role="tab">' + blockName + '</a></li>');
    }
    if(block.hasClass('push-right')) {
        item.addClass('ml-auto')
    }
    blockSwitch.append(item);
    return {'item': item, 'content': content};
}

$(addBlockSwitches);