package io.payworks;

import android.content.Context;
import android.content.Intent;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.EnumSet;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;

public class MposUIPlugin extends CordovaPlugin {

	private CallbackContext callbackContext;

	@Override
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;

		if(action.equals("transaction")) {
			MposUi ui = MposUi.initialize(cordova.getActivity(), ProviderMode.MOCK,
					"merchantIdentifier", "merchantSecretKey");

            ui.getConfiguration().setSummaryFeatures(EnumSet.of(
							// Add this line, if you do want to offer printed receipts
							// MposUiConfiguration.SummaryFeature.PRINT_RECEIPT,
							MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL)
			);

   			AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MOCK)
                                                            .bluetooth()
															  .build();
			// using a real device
			//AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MIURA_MPI).bluetooth().build();
    		ui.getConfiguration().setTerminalParameters(accessoryParameters);


    		TransactionParameters transactionParameters = new TransactionParameters.Builder()
                                                            .charge(new BigDecimal("5.00"), io.mpos.transactions.Currency.EUR)
                                                            .subject("Bouquet of Flowers")
                                                            .customIdentifier("yourReferenceForTheTransaction")
                                                            .build();

    		Intent intent = ui.createTransactionIntent(transactionParameters);
			cordova.setActivityResultCallback(this);
			cordova.getActivity().startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
		}



		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == MposUi.REQUEST_CODE_PAYMENT) {
			if (resultCode == MposUi.RESULT_CODE_APPROVED) {
				// Transaction was approved
				callbackContext.success("Transaction approved");
			} else {
				// Card was declined, or transaction was aborted, or failed
				// (e.g. no internet or accessory not found)
				callbackContext.success("Transaction declined");
			}
			// Grab the processed transaction in case you need it
			// (e.g. the transaction identifier for a refund).
			// Keep in mind that the returned transaction might be null
			// (e.g. if it could not be registered).
			//Transaction transaction = MposUi.getInitializedInstance().getTransaction();
		}
	}

}
