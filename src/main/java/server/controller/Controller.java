package server.controller;

import common.*;
import server.integration.FileDAO;
import server.model.File;
import server.model.User;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Controller extends UnicastRemoteObject implements ServerReacher {

    private final FileDAO fileDAO = new FileDAO();
    private ClientReacher remoteObject;
    private User user;

    public Controller() throws RemoteException {}

    @Override
    public void logIn(ClientReacher remoteObject, LogInDetails lid ) throws UserError {
        this.remoteObject = remoteObject;
        this.user = fileDAO.checkLogin(lid);
    }

    @Override
    public void logOut(LogInDetails lgn){
        remoteObject = null;
        user = null;
    }

    @Override
    public void register(LogInDetails lgn) throws UserError {
        user = fileDAO.createUser(lgn);
    }

    @Override
    public void unRegister(LogInDetails lid) throws UserError {
        if(!lid.getUsername().equals(user.getUsername()))
            throw new UserError("Username mismatch, something went wrong with session please re-login.");
        fileDAO.deleteUser(user);
    }

    @Override
    public void fileUpload(java.io.File file, LogInDetails lid) throws FileError, UserError {
        if(!lid.getUsername().equals(user.getUsername()))
            throw new UserError("Username mismatch, something went wrong with session please re-login.");
        File serverFile = new File(file.getName(), user, file);
        fileDAO.createFile(serverFile);
    }

    @Override
    public void fileUpload(java.io.File file, boolean privateAccess, boolean writePermission, LogInDetails lid) throws FileError, UserError {
        if(!lid.getUsername().equals(user.getUsername()))
            throw new UserError("Username mismatch, something went wrong with session please re-login.");
        File serverFile = new File(file.getName(), user, privateAccess, writePermission, file);
        fileDAO.createFile(serverFile);

    }

    @Override
    public java.io.File fileDownload(String fileName, LogInDetails lid) throws FileError, UserError {
        if(!lid.getUsername().equals(user.getUsername()))
            throw new UserError("Username mismatch, something went wrong with session please re-login.");
        return fileDAO.getFile(fileName, user);
    }

    @Override
    public void setNotification(boolean notify, String file, LogInDetails lid) throws FileError, UserError {

    }

    public void deleteFile(String fileName, LogInDetails lid) throws FileError, UserError {
        if(!lid.getUsername().equals(user.getUsername()))
            throw new UserError("Username mismatch, something went wrong with session please re-login.");
        fileDAO.deleteFile(user, fileName);
    }

    public void togglePrivate(String fileName, LogInDetails lid) throws UserError, FileError {
        if(!lid.getUsername().equals(user.getUsername()))
            throw new UserError("Username mismatch, something went wrong with session please re-login.");
        fileDAO.togglePrivate(fileName, user);
    }

    private void startRegistry() throws RemoteException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException noRegistryIsRunning) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
    }
}