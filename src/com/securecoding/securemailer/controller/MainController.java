package com.securecoding.securemailer.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.*;
import com.securecoding.securemailer.App;
import com.securecoding.securemailer.engine.EmailEngine;
import com.securecoding.securemailer.engine.TemplateBuilder;
import com.securecoding.securemailer.model.Receiver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private JFXTextField fieldSubject, fieldSenderEmail;
    @FXML
    private JFXPasswordField fieldSenderPassword;

    @FXML
    private JFXComboBox<String> comboRecevicesFormatType;
    @FXML
    private JFXTextArea areaTo;

    @FXML
    private JFXTextField fieldHeaderTitle;
    @FXML
    private Label lblHeaderImgName;

    @FXML
    private JFXTextArea areaBody;

    @FXML
    private JFXTextField fieldFooterAbout;
    @FXML
    private JFXTextArea areaFooterContact;

    @FXML
    private WebView webViewTemplate;

    private File headerImg;

    private FileChooser imgChooser;

    private String htmlTemplate;
    
    private String default_img_path;

    /* Start sending status */

    @FXML
    private StackPane stackSendingContainer;

    @FXML
    private Label lblNumberOfSent, lblNumberOfReceivers, lblNumberOfSuccess, lblNumberOfFailed;

    @FXML
    private JFXProgressBar progressSending;

    @FXML
    private JFXButton btnDone;

    /* End sending status */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // init image chooser
        imgChooser = new FileChooser();
        FileChooser.ExtensionFilter imgChooserExtension = new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg", "*.gif");
        imgChooser.getExtensionFilters().add(imgChooserExtension);
        default_img_path = MainController.class.getResource("/resources/default_image.png").getPath();
    }

    @FXML
    private void onLoadHeaderImage() {
        headerImg = imgChooser.showOpenDialog(App.stage);
        if(headerImg != null)
            lblHeaderImgName.setText(headerImg.getName());
    }

    @FXML
    private void onPreview() {
        htmlTemplate = new TemplateBuilder()
                .setHeader(headerImg == null ? default_img_path : headerImg.getPath(), fieldHeaderTitle.getText())
                .setBody(areaBody.getText())
                .setFooter(fieldFooterAbout.getText(), areaFooterContact.getText())
                .build();
        
        webViewTemplate.getEngine().loadContent(htmlTemplate);
    }

    @FXML
    private void onSend() {	
        TemplateBuilder templateBuilder = new TemplateBuilder()
                .setHeader("cid:headerImage", fieldHeaderTitle.getText())
                .setFooter(fieldFooterAbout.getText(), areaFooterContact.getText());

        EmailEngine emailEngine = new EmailEngine()
                .setAuth(fieldSenderEmail.getText().trim(), fieldSenderPassword.getText())
                .setSubject(fieldSubject.getText());
        
        stackSendingContainer.setVisible(true);
        String[] receivers = areaTo.getText().trim().split(",");
        
        clearSendingStatus();
        int numberOfReceivers = receivers.length;
        lblNumberOfReceivers.setText(String.valueOf(numberOfReceivers));

        new Thread(()->
        Platform.runLater(() -> {
            int numberOfSent = 0;
            for(String receiver : receivers) {
               templateBuilder.setBody(areaBody.getText()
                        .replaceFirst("<name>", "<span style='color: #2196f3'>" + receiver + "</span>")
                        .replace("<name>", receiver));

                // init email engine
                emailEngine.setContent(templateBuilder.build())
                        .setHeaderImage(headerImg == null ? default_img_path : headerImg.getPath());

                boolean isSent = emailEngine.send(receiver);
                numberOfSent++;
                if (isSent) {
                    System.out.println(String.format("%s -> Success [%d/%d]", receiver, numberOfSent, numberOfReceivers));
                    lblNumberOfSuccess.setText(String.valueOf(Integer.parseInt(lblNumberOfSuccess.getText()) + 1));
                } else {
                    System.out.println(String.format("%s -> Failed [%d/%d]", receiver, numberOfSent, numberOfReceivers));
                    lblNumberOfFailed.setText(String.valueOf(Integer.parseInt(lblNumberOfFailed.getText()) + 1));
                }

                lblNumberOfSent.setText(String.valueOf(Integer.valueOf(lblNumberOfSent.getText()) + 1));
                progressSending.setProgress(Integer.valueOf(lblNumberOfSent.getText()) / (double) numberOfReceivers);

                btnDone.setDisable(false);
            }
        })
        ).start();
    }

    private void clearSendingStatus() {
        lblNumberOfSent.setText("0");
        lblNumberOfReceivers.setText(null);
        lblNumberOfSuccess.setText("0");
        lblNumberOfFailed.setText("0");
        progressSending.setProgress(0d);
        btnDone.setDisable(true);
    }

    /* sending status actions */

    @FXML
    private void onDone() {
        stackSendingContainer.setVisible(false);
    }

}
