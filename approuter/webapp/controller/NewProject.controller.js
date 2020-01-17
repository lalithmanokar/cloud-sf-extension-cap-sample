sap.ui.define([
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/mvc/Controller",
	"sap/ui/core/routing/History",
	"sap/ui/core/UIComponent"
], function (JSONModel, Controller, History, UIComponent) {
	"use strict";

	return Controller.extend("sap.ui.demo.myapp.controller.NewProject", {

		onInit: function (oEvent) {
			var dataObject = {
				projectName: "",
				description: ""
			};
			var oModel = new JSONModel();
			oModel.setData(dataObject);
			sap.ui.getCore().setModel(oModel);
		},

		onPressSubmit: function () {
			var oModel = sap.ui.getCore().getModel();
			var oProperty = oModel.getProperty("/");
			var data = {"projectName":this.getView().byId("projectName").getValue(), "description": this.getView().byId("description").getValue()};


			
		},

		getRouter: function () {
			return UIComponent.getRouterFor(this);
		},

		onNavBack: function () {
			var oHistory, sPreviousHash;

			oHistory = History.getInstance();
			sPreviousHash = oHistory.getPreviousHash();

			if (sPreviousHash !== undefined) {
				window.history.go(-1);
			} else {
				this.getRouter().navTo("master", {}, true /*no history*/ );
			}
		}
	});
});