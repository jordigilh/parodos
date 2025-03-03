/*
 * Parodos Notification Service API
 * This is the API documentation for the Parodos Notification Service. It provides operations to send out and check notification. The endpoints are secured with oAuth2/OpenID and cannot be accessed without a valid token.
 *
 * The version of the OpenAPI document: v1.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.redhat.parodos.notification.sdk.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;

public class NotificationMessageApi {

	private ApiClient localVarApiClient;

	private int localHostIndex;

	private String localCustomBaseUrl;

	public NotificationMessageApi() {
		this(Configuration.getDefaultApiClient());
	}

	public NotificationMessageApi(ApiClient apiClient) {
		this.localVarApiClient = apiClient;
	}

	public ApiClient getApiClient() {
		return localVarApiClient;
	}

	public void setApiClient(ApiClient apiClient) {
		this.localVarApiClient = apiClient;
	}

	public int getHostIndex() {
		return localHostIndex;
	}

	public void setHostIndex(int hostIndex) {
		this.localHostIndex = hostIndex;
	}

	public String getCustomBaseUrl() {
		return localCustomBaseUrl;
	}

	public void setCustomBaseUrl(String customBaseUrl) {
		this.localCustomBaseUrl = customBaseUrl;
	}

	/**
	 * Build call for create
	 * @param notificationMessageCreateRequestDTO (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call createCall(NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO,
			final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		}
		else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		}
		else {
			basePath = null;
		}

		Object localVarPostBody = notificationMessageCreateRequestDTO;

		// create path and map variables
		String localVarPath = "/api/v1/messages";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = { "application/json" };
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams,
				localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
				localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call createValidateBeforeCall(
			NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO, final ApiCallback _callback)
			throws ApiException {
		// verify the required parameter 'notificationMessageCreateRequestDTO' is set
		if (notificationMessageCreateRequestDTO == null) {
			throw new ApiException(
					"Missing the required parameter 'notificationMessageCreateRequestDTO' when calling create(Async)");
		}

		return createCall(notificationMessageCreateRequestDTO, _callback);

	}

	/**
	 * @param notificationMessageCreateRequestDTO (required)
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public void create(NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO) throws ApiException {
		createWithHttpInfo(notificationMessageCreateRequestDTO);
	}

	/**
	 * @param notificationMessageCreateRequestDTO (required)
	 * @return ApiResponse&lt;Void&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot
	 * deserialize the response body
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public ApiResponse<Void> createWithHttpInfo(NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO)
			throws ApiException {
		okhttp3.Call localVarCall = createValidateBeforeCall(notificationMessageCreateRequestDTO, null);
		return localVarApiClient.execute(localVarCall);
	}

	/**
	 * (asynchronously)
	 * @param notificationMessageCreateRequestDTO (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request
	 * body object
	 * @http.response.details
	 * <table summary="Response Details" border="1">
	 * <tr>
	 * <td>Status Code</td>
	 * <td>Description</td>
	 * <td>Response Headers</td>
	 * </tr>
	 * <tr>
	 * <td>201</td>
	 * <td>Created</td>
	 * <td>-</td>
	 * </tr>
	 * </table>
	 */
	public okhttp3.Call createAsync(NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO,
			final ApiCallback<Void> _callback) throws ApiException {

		okhttp3.Call localVarCall = createValidateBeforeCall(notificationMessageCreateRequestDTO, _callback);
		localVarApiClient.executeAsync(localVarCall, _callback);
		return localVarCall;
	}

}
