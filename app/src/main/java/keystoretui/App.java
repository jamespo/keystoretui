package keystoretui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class App {
    private final String keystorePath;
    private KeystoreService keystoreService;
    private String filterString = "";

    public App(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public void start() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        try (Screen screen = terminalFactory.createScreen()) {
            screen.startScreen();
            final WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);

            String password = TextInputDialog.showPasswordDialog(gui, "Password", "Enter password for keystore:", "changeit");
            if (password == null) {
                return;
            }

            try {
                this.keystoreService = new KeystoreService(keystorePath, password);
                showMainWindow(gui);
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Error", "Failed to load keystore:\n" + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMainWindow(WindowBasedTextGUI gui) throws Exception {
        final Window window = new BasicWindow("Keystore TUI Viewer: " + keystorePath);
        window.setHints(Collections.singleton(Window.Hint.CENTERED));

        Panel contentPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        contentPanel.addComponent(new Label("Select an alias to view details (press '/' to filter):"));

        Label filterLabel = new Label("Filter: [None]");
        contentPanel.addComponent(filterLabel);

        ActionListBox listBox = new ActionListBox(new TerminalSize(60, 15));
        List<String> allAliases = keystoreService.getAliases();
        
        updateListBox(gui, listBox, allAliases);
        
        if (allAliases.isEmpty()) {
            contentPanel.addComponent(new Label("No entries found in keystore."));
        } else {
            contentPanel.addComponent(listBox);
        }

        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
                if (keyStroke.getCharacter() != null && keyStroke.getCharacter() == '/') {
                    String newFilter = TextInputDialog.showDialog(gui, "Filter", "Enter filter text (case-insensitive):", filterString);
                    if (newFilter != null) {
                        filterString = newFilter;
                        filterLabel.setText("Filter: " + (filterString.isEmpty() ? "[None]" : filterString));
                        updateListBox(gui, listBox, allAliases);
                    }
                    deliverEvent.set(false); // We handled the event
                }
            }
        });

        contentPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        contentPanel.addComponent(new Button("Exit", window::close));

        window.setComponent(contentPanel);
        gui.addWindowAndWait(window);
    }

    private void updateListBox(WindowBasedTextGUI gui, ActionListBox listBox, List<String> allAliases) {
        listBox.clearItems();
        List<String> filteredAliases = allAliases.stream()
                .filter(alias -> alias.toLowerCase().contains(filterString.toLowerCase()))
                .collect(Collectors.toList());

        for (String alias : filteredAliases) {
            listBox.addItem(alias, () -> showDetails(gui, alias));
        }
    }

    private void showDetails(WindowBasedTextGUI gui, String alias) {
        try {
            String details = keystoreService.getEntryDetails(alias);
            MessageDialog.showMessageDialog(gui, "Entry Details", details);
        } catch (Exception e) {
            MessageDialog.showMessageDialog(gui, "Error", "Failed to get details: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String path = (args.length > 0) ? args[0] : "java-cacerts.jks";
        App app = new App(path);
        try {
            app.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
