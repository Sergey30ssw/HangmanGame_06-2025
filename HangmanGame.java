import java.util.*;
import java.io.*;
import java.nio.file.*;

public class HangmanGame {
    private static final String WORDS_FILE = "words.txt";
    private static final String SCORES_FILE = "hangman_scores.txt";
    private static List<String> words = new ArrayList<>();
    private static int totalGames = 0;
    private static int wins = 0;
    private static int losses = 0;
    private static String playerName = "";
    private static boolean hintUsed = false;

    public static void main(String[] args) {
        loadWords();
        loadScores();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в игру Виселица!");
        System.out.print("Введите ваше имя: ");
        playerName = scanner.nextLine();

        boolean playAgain = true;
        while (playAgain) {
            Difficulty difficulty = chooseDifficulty(scanner);
            playGame(scanner, difficulty);

            System.out.print("\nХотите сыграть еще раз? (да/нет): ");
            String response = scanner.nextLine().toLowerCase();
            playAgain = response.equals("да") || response.equals("д");
        }

        saveScores();
        System.out.println("\nСпасибо за игру! Ваша статистика:");
        printStatistics();
        scanner.close();
    }

    private static Difficulty chooseDifficulty(Scanner scanner) {
        System.out.println("\nВыберите уровень сложности:");
        System.out.println("1 - Легкий (10 попыток)");
        System.out.println("2 - Средний (7 попыток)");
        System.out.println("3 - Сложный (5 попыток)");

        while (true) {
            System.out.print("Ваш выбор (1-3): ");
            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    return new Difficulty("Легкий", 10);
                case "2":
                    return new Difficulty("Средний", 7);
                case "3":
                    return new Difficulty("Сложный", 5);
                default:
                    System.out.println("Пожалуйста, введите число от 1 до 3");
            }
        }
    }

    private static void playGame(Scanner scanner, Difficulty difficulty) {
        if (words.isEmpty()) {
            System.out.println("Ошибка: нет слов для игры. Добавьте слова в файл words.txt");
            return;
        }

        Random random = new Random();
        String secretWord = words.get(random.nextInt(words.size())).toLowerCase();
        char[] guessedLetters = new char[secretWord.length()];
        Arrays.fill(guessedLetters, '_');

        int triesLeft = difficulty.getMaxTries();
        Set<Character> usedLetters = new HashSet<>();
        hintUsed = false;

        System.out.println("\nНовая игра! Уровень: " + difficulty.getName());
        System.out.println("Угадайте слово. У вас " + triesLeft + " попыток.");
        System.out.println("Введите 'подсказка' для получения подсказки (но потеряете 2 попытки).");

        while (triesLeft > 0 && !isWordGuessed(guessedLetters)) {
            printGameState(guessedLetters, triesLeft, usedLetters, difficulty);

            System.out.print("Введите букву или 'подсказка': ");
            String input = scanner.nextLine().toLowerCase();

            if (input.equals("подсказка")) {
                if (!hintUsed) {
                    giveHint(secretWord, guessedLetters);
                    triesLeft = Math.max(0, triesLeft - 2);
                    hintUsed = true;
                    System.out.println("Использована подсказка! Осталось попыток: " + triesLeft);
                } else {
                    System.out.println("Вы уже использовали подсказку в этой игре.");
                }
                continue;
            }

            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                System.out.println("Пожалуйста, введите одну букву или 'подсказка'.");
                continue;
            }

            char letter = input.charAt(0);

            if (usedLetters.contains(letter)) {
                System.out.println("Вы уже пробовали эту букву.");
                continue;
            }

            usedLetters.add(letter);

            if (secretWord.indexOf(letter) >= 0) {
                for (int i = 0; i < secretWord.length(); i++) {
                    if (secretWord.charAt(i) == letter) {
                        guessedLetters[i] = letter;
                    }
                }
                System.out.println("Верно! Буква '" + letter + "' есть в слове.");
            } else {
                triesLeft--;
                System.out.println("Неверно! Буквы '" + letter + "' нет в слове. Осталось попыток: " + triesLeft);
                drawHangman(triesLeft, difficulty.getMaxTries());
            }
        }

        totalGames++;
        if (isWordGuessed(guessedLetters)) {
            wins++;
            System.out.println("\nПоздравляем! Вы угадали слово: " + secretWord);
        } else {
            losses++;
            System.out.println("\nК сожалению, вы проиграли. Загаданное слово было: " + secretWord);
            drawHangman(0, difficulty.getMaxTries());
        }
    }

    private static void giveHint(String secretWord, char[] guessedLetters) {
        List<Integer> hiddenIndices = new ArrayList<>();
        for (int i = 0; i < guessedLetters.length; i++) {
            if (guessedLetters[i] == '_') {
                hiddenIndices.add(i);
            }
        }

        if (!hiddenIndices.isEmpty()) {
            int randomIndex = hiddenIndices.get(new Random().nextInt(hiddenIndices.size()));
            guessedLetters[randomIndex] = secretWord.charAt(randomIndex);
            System.out.println("Подсказка: открыта буква '" + secretWord.charAt(randomIndex) + "'");
        }
    }

    private static boolean isWordGuessed(char[] guessedLetters) {
        for (char c : guessedLetters) {
            if (c == '_') {
                return false;
            }
        }
        return true;
    }

    private static void printGameState(char[] guessedLetters, int triesLeft, Set<Character> usedLetters, Difficulty difficulty) {
        System.out.println("\nСлово: " + String.valueOf(guessedLetters));
        System.out.println("Использованные буквы: " + usedLetters);
        System.out.println("Осталось попыток: " + triesLeft + "/" + difficulty.getMaxTries());
    }

    private static void drawHangman(int triesLeft, int maxTries) {
        int wrongAttempts = maxTries - triesLeft;

        System.out.println("  ____");
        System.out.println(" |    |");

        if (wrongAttempts > 0) {
            System.out.println(" |    O");
        } else {
            System.out.println(" |");
        }

        if (wrongAttempts > 3) {
            System.out.println(" |   /|\\");
        } else if (wrongAttempts > 2) {
            System.out.println(" |   /|");
        } else if (wrongAttempts > 1) {
            System.out.println(" |    |");
        } else {
            System.out.println(" |");
        }

        if (wrongAttempts > 5) {
            System.out.println(" |   / \\");
        } else if (wrongAttempts > 4) {
            System.out.println(" |   /");
        } else {
            System.out.println(" |");
        }

        System.out.println("_|_");
    }

    private static void loadWords() {
        try {
            words = Files.readAllLines(Paths.get(WORDS_FILE));
            // Удаляем пустые строки и строки только с пробелами
            words.removeIf(line -> line.trim().isEmpty());

            if (words.isEmpty()) {
                System.out.println("Файл words.txt пуст. Будут использованы слова по умолчанию.");
                loadDefaultWords();
            }
        } catch (IOException e) {
            System.out.println("Файл words.txt не найден или недоступен. Будут использованы слова по умолчанию.");
            loadDefaultWords();
        }
    }

    private static void loadDefaultWords() {
        words = Arrays.asList(
                "целеполагание", "осознанность", "необходимость",
                "солипсизм", "расстановка", "виртуоз",
                "стратегия", "стоицизм", "искусственный",
                "пубертат", "протекционизм", "абьюзер",
                "похлебка", "прерогатива", "величество",
                "зеркало", "отображение", "негодяй"
        );
    }

    private static void loadScores() {
        try (Scanner fileScanner = new Scanner(new File(SCORES_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.startsWith(playerName + "|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4) {
                        totalGames = Integer.parseInt(parts[1]);
                        wins = Integer.parseInt(parts[2]);
                        losses = Integer.parseInt(parts[3]);
                    }
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            // Файл не найден - начнем с нулевыми результатами
        }
    }

    private static void saveScores() {
        List<String> lines = new ArrayList<>();
        boolean playerFound = false;

        // Читаем существующие записи
        try (Scanner fileScanner = new Scanner(new File(SCORES_FILE))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.startsWith(playerName + "|")) {
                    line = playerName + "|" + totalGames + "|" + wins + "|" + losses;
                    playerFound = true;
                }
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            // Файл не существует - создадим новый
        }

        // Если игрок не найден в файле, добавляем новую запись
        if (!playerFound) {
            lines.add(playerName + "|" + totalGames + "|" + wins + "|" + losses);
        }

        // Записываем обновленные данные обратно в файл
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORES_FILE))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении результатов: " + e.getMessage());
        }
    }

    private static void printStatistics() {
        System.out.println("Игрок: " + playerName);
        System.out.println("Всего игр: " + totalGames);
        System.out.println("Побед: " + wins);
        System.out.println("Поражений: " + losses);

        if (totalGames > 0) {
            double winRate = (double) wins / totalGames * 100;
            System.out.printf("Процент побед: %.1f%%\n", winRate);
        }
    }

    static class Difficulty {
        private String name;
        private int maxTries;

        public Difficulty(String name, int maxTries) {
            this.name = name;
            this.maxTries = maxTries;
        }

        public String getName() {
            return name;
        }

        public int getMaxTries() {
            return maxTries;
        }
    }
}
