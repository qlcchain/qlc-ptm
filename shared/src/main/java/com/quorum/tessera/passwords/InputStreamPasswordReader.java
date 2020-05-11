package com.quorum.tessera.passwords;

import java.io.InputStream;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

public class InputStreamPasswordReader implements PasswordReader {

    private final Scanner inputStream;

    public InputStreamPasswordReader(final InputStream inputStream) {
        this.inputStream = new Scanner(requireNonNull(inputStream));
    }

    @Override
    public char[] readPasswordFromConsole() {
        if (this.inputStream.hasNextLine()) {
            return this.inputStream.nextLine().toCharArray();
        } else {
            return new char[0];
        }
    }

}
