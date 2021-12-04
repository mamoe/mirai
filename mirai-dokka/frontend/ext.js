// noinspection ES6ConvertVarToLetConst,JSUnresolvedVariable

/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */



(function () {
    fetch(window.pathToRoot + "../versions.json").then(function (it) {
        return it.json()
    }).then(function (rsp) {
        console.log(rsp);
        var dir = document.getElementById("searchBar").parentElement;
        var select = document.createElement("select");
        dir.insertBefore(select, dir.firstElementChild);
        select.appendChild(document.createElement("option")).textContent = "other version";
        var toLatest = select.appendChild(document.createElement("option"));
        toLatest.textContent = "latest";
        toLatest.value = "";
        for (var v of rsp) {
            var c = select.appendChild(document.createElement("option"));
            c.textContent = v;
            c.value = v;
        }
        select.addEventListener("change", function (event) {
            location.href = window.pathToRoot + "../" + c.value
        })
    }).catch(function (error) {
        console.log(error);
    })
})()
