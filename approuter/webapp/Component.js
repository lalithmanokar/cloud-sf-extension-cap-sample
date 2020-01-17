sap.ui.define([
	"sap/ui/core/UIComponent",
	"sap/ui/model/json/JSONModel",
	"sap/f/library",
	'sap/f/FlexibleColumnLayoutSemanticHelper',
], function (UIComponent, JSONModel, fioriLibrary, FlexibleColumnLayoutSemanticHelper) {
	"use strict";

	return UIComponent.extend("sap.ui.demo.myapp.Component", {

		metadata: {
			manifest: "json"
		},

		/**
		 * The component is initialized by UI5 automatically during the startup of the app and calls the init method once.
		 * @public
		 * @override
		 */
		
		init: function () {
			var oProjectsModel, oModel, oRouter;

			UIComponent.prototype.init.apply(this, arguments);
			
			oModel = new JSONModel();
			this.setModel(oModel);

			// set projects demo model on this sample
			//oProjectsModel = new JSONModel(sap.ui.require.toUrl('sap/ui/demo/mock') + '/products.json');
			oProjectsModel = new JSONModel( '/sf-extension.svc/api/v1/projects');
			oProjectsModel.setSizeLimit(1000);
			this.setModel(oProjectsModel, 'projects');
			
			oRouter = this.getRouter();
			oRouter.attachBeforeRouteMatched(this._onBeforeRouteMatched, this);
			oRouter.initialize();
		},
		
		getHelper: function () {
			return this._getFcl().then(function(oFCL) {
				var oSettings = {
					defaultTwoColumnLayoutType: fioriLibrary.LayoutType.TwoColumnsMidExpanded,
					defaultThreeColumnLayoutType: fioriLibrary.LayoutType.ThreeColumnsMidExpanded
					//initialColumnsCount: 2 //enable to make detail page appear by default
					//maxColumnsCount: 2 //to have third column appear as new page
				};
				return (FlexibleColumnLayoutSemanticHelper.getInstanceFor(oFCL, oSettings));
			});
		},
		
		_onBeforeRouteMatched: function(oEvent) {
			var oModel = this.getModel(),
				sLayout = oEvent.getParameters().arguments.layout, oNextUIState;

			// If there is no layout parameter, query for the default level 0 layout  (normally OneColumn)
			if (!sLayout) {
				//sLayout = fioriLibrary.LayoutType.OneColumn;
				this.getHelper().then(function(oHelper) {
					oNextUIState = oHelper.getNextUIState(0);
					oModel.setProperty("/layout", oNextUIState.layout);
				});
				return;
			}

			oModel.setProperty("/layout", sLayout);
		},
		
		_getFcl: function () {
			return new Promise(function(resolve, reject) {
				var oFCL = this.getRootControl().byId('flexibleColumnLayout');
				if (!oFCL) {
					this.getRootControl().attachAfterInit(function(oEvent) {
						resolve(oEvent.getSource().byId('flexibleColumnLayout'));
					}, this);
					return;
				}
				resolve(oFCL);

			}.bind(this));
		}
	});
});