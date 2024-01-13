import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GestionareMasini extends JFrame {
    private JTextField textFieldNume;
    private JTextField textFieldCod;
    private JButton btnIdentificare;
    private JButton btnAdaugaMasina;
    private JButton btnStergeMasina; // Butonul pentru stergerea masinii
    private JTextArea textAreaMasini;

    private String utilizatorCurent = null;
    private JSONArray bazaDeDateMasini;

    public GestionareMasini() {
        setTitle("Gestionare Masini");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        layoutComponents();
        addListeners();

        // La deschiderea programului, incarcam baza de date
        bazaDeDateMasini = citesteBazaDeDate();
    }

    private void initComponents() {
        textFieldNume = new JTextField(20);
        textFieldCod = new JTextField(10);
        btnIdentificare = new JButton("Identificare");
        btnAdaugaMasina = new JButton("Adauga Masina");
        btnStergeMasina = new JButton("Sterge Masina"); // Initializam butonul pentru stergerea masinii
        textAreaMasini = new JTextArea(10, 30);
        textAreaMasini.setEditable(false);
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5)); // Am adaugat un rand in plus pentru butonul de stergere
        panel.add(new JLabel("Nume:"));
        panel.add(textFieldNume);
        panel.add(new JLabel("Cod:"));
        panel.add(textFieldCod);
        panel.add(btnIdentificare);
        panel.add(btnAdaugaMasina);
        panel.add(btnStergeMasina); // Adaugam butonul de stergere
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(textAreaMasini), BorderLayout.CENTER);
    }

    private void addListeners() {
        btnIdentificare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                identificareUtilizator();
            }
        });

        btnAdaugaMasina.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adaugaMasina();
            }
        });

        btnStergeMasina.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stergeMasina();
            }
        });
    }

    private void identificareUtilizator() {
        String nume = normalizareCaractere(textFieldNume.getText());
        String cod = textFieldCod.getText();

        if (!nume.isEmpty() && !cod.isEmpty()) {
            utilizatorCurent = nume + "_" + cod;
            afiseazaMasini();
        } else {
            JOptionPane.showMessageDialog(this, "Introduceti un nume si codul pentru a va identifica.");
        }
    }

    private String normalizareCaractere(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void adaugaMasina() {
        if (utilizatorCurent != null) {
            String marca = JOptionPane.showInputDialog("Introduceti marca masinii:");
            String model = JOptionPane.showInputDialog("Introduceti modelul masinii:");
            String anFabricatie = JOptionPane.showInputDialog("Introduceti anul de fabricatie al masinii:");

            JSONObject masina = new JSONObject();
            masina.put("Marca", marca);
            masina.put("Model", model);
            masina.put("AnFabricatie", anFabricatie);

            JSONArray listaMasini = getListaMasini(utilizatorCurent);
            listaMasini.add(masina);

            salveazaBazaDeDate();
            afiseazaMasini();
        } else {
            JOptionPane.showMessageDialog(this, "Identificati-va mai intai pentru a adauga o masina.");
        }
    }

    private void stergeMasina() {
        if (utilizatorCurent != null) {
            String marcaDeStergut = JOptionPane.showInputDialog("Introduceti marca masinii de sters:");

            JSONArray listaMasini = getListaMasini(utilizatorCurent);

            for (Object obj : listaMasini) {
                JSONObject masina = (JSONObject) obj;
                String marca = (String) masina.get("Marca");

                if (marca.equals(marcaDeStergut)) {
                    listaMasini.remove(masina);
                    salveazaBazaDeDate();
                    afiseazaMasini();
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Masina cu marca " + marcaDeStergut + " nu a fost gasita.");
        } else {
            JOptionPane.showMessageDialog(this, "Identificati-va mai intai pentru a sterge o masina.");
        }
    }

    private void afiseazaMasini() {
        textAreaMasini.setText("");
        JSONArray listaMasini = getListaMasini(utilizatorCurent);

        for (Object obj : listaMasini) {
            JSONObject masina = (JSONObject) obj;
            textAreaMasini.append("Marca: " + masina.get("Marca") + ", Model: " + masina.get("Model") +
                    ", An Fabricatie: " + masina.get("AnFabricatie") + "\n");
        }
    }

    @SuppressWarnings("unchecked")
    private JSONArray citesteBazaDeDate() {
        try (FileReader reader = new FileReader("baza_de_date.json")) {
            return (JSONArray) new org.json.simple.parser.JSONParser().parse(reader);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void salveazaBazaDeDate() {
        try (FileWriter file = new FileWriter("baza_de_date.json")) {
            file.write(bazaDeDateMasini.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray getListaMasini(String utilizator) {
        for (Object obj : bazaDeDateMasini) {
            JSONObject entry = (JSONObject) obj;
            String user = (String) entry.get("Utilizator");
            if (user.equals(utilizator)) {
                return (JSONArray) entry.get("Masini");
            }
        }

        JSONObject entry = new JSONObject();
        entry.put("Utilizator", utilizator);
        JSONArray listaMasini = new JSONArray();
        entry.put("Masini", listaMasini);
        bazaDeDateMasini.add(entry);

        return listaMasini;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GestionareMasini().setVisible(true);
            }
        });
    }
}
