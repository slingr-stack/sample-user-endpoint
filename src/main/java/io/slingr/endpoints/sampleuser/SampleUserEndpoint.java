package io.slingr.endpoints.sampleuser;

import io.slingr.endpoints.PerUserEndpoint;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.framework.annotations.*;
import io.slingr.endpoints.sampleuser.services.HttpHelper;
import io.slingr.endpoints.services.AppLogs;
import io.slingr.endpoints.services.datastores.DataStoreResponse;
import io.slingr.endpoints.services.exchange.ReservedName;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.ws.exchange.FunctionRequest;
import io.slingr.endpoints.ws.exchange.WebServiceRequest;
import io.slingr.endpoints.ws.exchange.WebServiceResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * <p>Sample User endpoint
 *
 * <p>Created by lefunes on 06/12/16.
 */
@SlingrEndpoint(name = "sample-user")
public class SampleUserEndpoint extends PerUserEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(SampleUserEndpoint.class);

    @ApplicationLogger
    private AppLogs appLogs;

    @EndpointProperty
    private String token;

    private final Random random = new Random();

    /**
     * Global function: it does not verify the user before to execute the request
     */
    @EndpointFunction
    public Json globalFunction(FunctionRequest request){
        final Json data = request.getJsonParams();
        appLogs.info("Request to GLOBAL FUNCTION received", data);

        data.set("token", token);
        data.set("number", random.nextInt(10000));

        logger.info(String.format("Function GLOBAL FUNCTION: [%s]", data.toString()));
        return data;
    }

    /**
     * User function: it checks if the user that generate the function request is connected
     */
    @EndpointFunction
    public Json userFunction(FunctionRequest request){
        final Json data = request.getJsonParams();
        appLogs.info("Request to USER FUNCTION received", data);

        data.set("token", token);
        data.set("number", random.nextInt(10000));
        data.set("userConnected", false);

        final String userId = request.getUserId();
        try {
            Json conf = users().checkUserConnection(userId, request.getJsonParams());
            data.set("userIdentifier", userId)
                    .set("userConnected", true)
                    .set("userCode", conf.string("code"));
        } catch (EndpointException ex){
            appLogs.error(ex.getMessage(), ex);

            // send disconnect event
            users().sendUserDisconnectedEvent(userId);
        }

        logger.info(String.format("Function USER FUNCTION: [%s]", data.toString()));
        return data;
    }

    /**
     * Main page: GET /
     * Returns a menu with buttons to generate and send the global and user event to the app
     */
    @EndpointWebService
    public WebServiceResponse mainPage(WebServiceRequest request){
        final StringBuilder sb = new StringBuilder();
        try {
            showButtons(sb, properties().getWebServicesUri());
        } catch (Exception ex){
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to check users: </b><br />%s", ex.getMessage()));
        }
        return new WebServiceResponse(HttpHelper.formatPage(sb), ContentType.TEXT_HTML.toString());
    }

    /**
     * Global event: GET /global
     * Sends a 'global event'. It does not check the users
     */
    @EndpointWebService(path = "global")
    public WebServiceResponse globalEventPage(WebServiceRequest request){
        final StringBuilder sb = new StringBuilder();
        try {
            appLogs.info("Sending global event to app");
            events().send("globalEvent", Json.map()
                    .set("token", token)
                    .set("number", random.nextInt(10000))
            );
            HttpHelper.addAlert(sb, "success", "Global event sent!");

            showButtons(sb, properties().getWebServicesUri());
        } catch (Exception ex){
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to check users: </b><br />%s", ex.getMessage()));
        }
        return new WebServiceResponse(HttpHelper.formatPage(sb), ContentType.TEXT_HTML.toString());
    }

    /**
     * User event: GET /user/{user id}
     * Sends a 'user event' if the user is connected
     */
    @EndpointWebService(path = "user/{userId}")
    public WebServiceResponse userEventsPage(WebServiceRequest request){
        final StringBuilder sb = new StringBuilder();
        try {
            final String userId = request.getPathVariable("userId");
            try {
                Json conf = users().checkUserConnection(userId, request.getJsonBody());
                HttpHelper.addAlert(sb, "success", String.format("User connected: %s", userId));

                appLogs.info(String.format("Sending user event to app. User [%s]", userId));
                Json data = Json.map()
                        .set("token", token)
                        .set("number", random.nextInt(10000))
                        .set("userIdentifier", userId)
                        .set("userConnected", true)
                        .set("userCode", conf.string("code"));

                events().send("userEvent", data, null, userId);
                HttpHelper.addAlert(sb, "success", String.format("User event sent to [%s]!", userId));
            } catch (Exception ex){
                appLogs.error(ex.getMessage(), ex);
                HttpHelper.addAlert(sb, "danger", ex.getMessage());

                // send disconnect event
                users().sendUserDisconnectedEvent(userId);
                HttpHelper.addAlert(sb, "success", String.format("User disconnected event: %s", userId));
            }

            showButtons(sb, properties().getWebServicesUri());
        } catch (Exception ex){
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to check users: </b><br />%s", ex.getMessage()));
        }
        return new WebServiceResponse(HttpHelper.formatPage(sb), ContentType.TEXT_HTML.toString());
    }

    /**
     * Connect user function: processor for the 'connect user' request. Generates a 'user connected' or 'user disconnected' event as result
     */
    @EndpointFunction(name = ReservedName.CONNECT_USER)
    public Json connectUser(FunctionRequest request) {
        // gets the user that pushed 'Connect To Service'
        final String userId = request.getUserId();
        if(StringUtils.isNotBlank(userId)) {

            // checks if the user includes a non-empty 'code' on the request
            if (StringUtils.isNotBlank(request.getJsonParams().string("code"))) {
                // saves the information on the users data store
                Json conf = users().save(userId, request.getJsonParams());
                logger.info(String.format("User connected [%s] [%s]", userId, conf.toString()));

                // sends connected user event
                users().sendUserConnectedEvent(request.getFunctionId(), userId, conf);

                return conf;
            } else {
                logger.info(String.format("Empty 'code' when try to connect user [%s] [%s]", userId, request.getParams().toString()));
            }
        }
        // user is not connected
        defaultMethodDisconnectUsers(request);
        return Json.map();
    }

    private void showButtons(StringBuilder sb, String webhookUrl){
        // shows button to send a global event
        HttpHelper.addButton(webhookUrl, "global", sb, "primary", "Global event");

        // shows buttons to send user events (one for each user)
        final DataStoreResponse response = userDataStore().find();
        if(response == null || response.getItems().isEmpty()){
            HttpHelper.addAlert(sb, "warning", "There is not user connected");
        } else {
            for (Json user : response.getItems()) {
                String userId = user.string("_id");
                HttpHelper.addButton(webhookUrl, String.format("user/%s", userId), sb, "info", String.format("User event: %s", userId));
            }
        }

        // shows a button to try to send an user event with invalid id
        HttpHelper.addButton(webhookUrl, "user/__invalid__user__", sb, "danger", "Invalid user event");
    }
}
