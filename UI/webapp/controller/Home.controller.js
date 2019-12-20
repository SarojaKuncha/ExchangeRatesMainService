sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/m/MessageBox"
], function (Controller, MessageBox) {
	"use strict";
	var homeView;

	return Controller.extend("UI.UI.controller.Home", {
		onInit: function () {

		},
		onInsert: function () {
			var url = "/fxrates/addAPI";
			var oEntity = {};
			/*	var basecurrency = this.getView().byId("apiName").getValue();
				var tocurrency = this.getView().byId("apiUrl").getValue();*/
			oEntity.apiName = homeView.byId("apiName").getValue();
			oEntity.apiUrl = homeView.byId("apiUrl").getValue();
			console.log(tocurrency);
			$.ajax({
				url: url,
				contentType: 'application/json; charset=UTF-8',
				type: 'POST',
				data: JSON.stringify(oEntity),
				async: false,
				cache: false,
				success: function (res) {
					MessageBox.confirm(
						"Record Inserted Sucessfully", {});
				},
				error: function (res) {
					MessageBox.confirm(
						"Failed to insert record", {});
				}
			});
		}
	});
});