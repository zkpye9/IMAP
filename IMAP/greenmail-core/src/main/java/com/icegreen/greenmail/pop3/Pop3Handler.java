/*
 * Copyright (c) 2014 Wael Chatila / Icegreen Technologies. All Rights Reserved.
 * This software is released under the Apache license 2.0
 * This file has been used and modified.
 * Original file can be found on http://foedus.sourceforge.net
 */
package com.icegreen.greenmail.pop3;


import com.icegreen.greenmail.pop3.commands.Pop3Command;
import com.icegreen.greenmail.pop3.commands.Pop3CommandRegistry;
import com.icegreen.greenmail.server.BuildInfo;
import com.icegreen.greenmail.server.ProtocolHandler;
import com.icegreen.greenmail.user.UserManager;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;


public class Pop3Handler implements ProtocolHandler {
    Pop3CommandRegistry _registry;
    Pop3Connection _conn;
    UserManager _manager;
    Pop3State _state;
    boolean _quitting;
    String _currentLine;
    private Socket _socket;

    public Pop3Handler(Pop3CommandRegistry registry,
                       UserManager manager, Socket socket) {
        _registry = registry;
        _manager = manager;
        _socket = socket;
    }

    @Override
    public void run() {
        try {
            _conn = new Pop3Connection(this, _socket);
            _state = new Pop3State(_manager);

            _quitting = false;

            sendGreetings();

            while (!_quitting) {
                handleCommand();
            }

            _conn.close();
        } catch (SocketTimeoutException ste) {
            _conn.println("421 Service shutting down and closing transmission channel");

        } catch (Exception e) {
        } finally {
            try {
                _socket.close();
            } catch (IOException ioe) {
            }
        }

    }

    void sendGreetings() {
        _conn.println("+OK POP3 GreenMail Server v" + BuildInfo.INSTANCE.getProjectVersion() + " ready");
    }

    void handleCommand()
            throws IOException {
        _currentLine = _conn.readLine();

        if (_currentLine == null) {
            close();

            return;
        }

        String commandName = new StringTokenizer(_currentLine, " ").nextToken()
                .toUpperCase();

        Pop3Command command = _registry.getCommand(commandName);

        if (command == null) {
            _conn.println("-ERR Command not recognized");

            return;
        }

        if (!command.isValidForState(_state)) {
            _conn.println("-ERR Command not valid for this state");

            return;
        }

        command.execute(_conn, _state, _currentLine);
    }

    @Override
    public void close() {
         _quitting = true;
        try {
            if (_socket != null && !_socket.isClosed()) {
                _socket.close();
            }
        } catch(IOException ignored) {
            //empty
        }
    }
}