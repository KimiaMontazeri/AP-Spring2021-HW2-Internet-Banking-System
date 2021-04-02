package com.company;
import java.util.Scanner;
import java.util.UUID;

public class Main
{
    private BankingSystem bank;
    private User currentUser;
    private Account currentAccount;
    private boolean sysadmin;

    public void mainMenu()
    {
        String userChoice;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("1.Sign up");
            System.out.println("2.Log in");
            System.out.println("3.System admin");
            System.out.println("4.Exit");
            userChoice = scanner.nextLine();

            switch (userChoice) {
                case "1" -> signUp();
                case "2" -> logIn();
                case "3" -> sysadminLogIn();
                case "4" -> System.out.println("Bye :))");
                default -> System.out.println("Invalid input.");
            }
        }while(!userChoice.equals("4"));
    }

    public void signUp()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1.Continue\n2.Back");
        if (scanner.nextLine().equals("2"))
            mainMenu();

        System.out.println("Enter info: (firstname, lastname, ID, password)");

        String firstname = scanner.nextLine();
        String lastname = scanner.nextLine();
        String ID = scanner.nextLine();
        String password = scanner.nextLine();

        User newUser = new User(firstname, lastname, ID, password);
        if (bank.register(newUser))
            mainMenu();
        else
        {
            System.out.println("1.Try again\n2.Back");
            if (scanner.nextLine().equals("1"))
                signUp();
            else
                mainMenu();
        }
    }

    public void logIn()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1.Continue\n2.Back");
        if (scanner.nextLine().equals("2"))
            mainMenu();

        System.out.println("Enter ID and password:");
        String ID = scanner.nextLine();
        String password = scanner.nextLine();

        currentUser = bank.login(ID, password);
        if (currentUser == null)
        {
            System.out.println("1.Try again\n2.Back");
            if (scanner.nextLine().equals("1"))
                logIn();
            else
                mainMenu();
        }
        loggedInUserMenu();
    }

    public void loggedInUserMenu()
    {
        // check if we're logged in
        if (currentUser == null)
            logIn();

        Scanner scanner = new Scanner(System.in);
        System.out.println("1.Existing accounts\n2.Add new account\n3.Log out");

        switch (scanner.nextLine()) {
            case "1" -> existingAccounts();
            case "2" -> {
                System.out.println("Enter your user ID and the account type:");
                String ID = scanner.nextLine();
                String type = scanner.nextLine();
                Account account = new Account(ID, currentUser.getFirstName(), currentUser.getLastname(), type);
                currentUser.addAccount(account);
                bank.addAccount(account);
                loggedInUserMenu();
            }
            case "3" -> {
                currentUser = null;
                System.out.println("Logged out of user");
                System.out.println("You will be forwarded back to log in menu...");
                logIn();
            }
            default -> loggedInUserMenu();
        }
    }

    public void existingAccounts()
    {
        Scanner scanner = new Scanner(System.in);
        currentUser.printAllAvailableAccounts();

        System.out.println("Enter the chosen account number or enter 0 to go back:");
        int index = scanner.nextInt() - 1;
        if (index == -1)
            loggedInUserMenu();

        currentAccount = currentUser.getAccountByIndex(index);
        while (currentAccount == null)
        {
            System.out.println("Invalid input. Try again:");
            currentAccount = currentUser.getAccountByIndex(scanner.nextInt() - 1);
        }
        System.out.println("Logged into account.");
        loggedInAccountMenu();
    }

    public void loggedInAccountMenu()
    {
        // check if we're logged in
        if (currentUser == null)
        {
            System.out.println("You haven't logged into a user yet.");
            logIn();
        }
        if (currentAccount == null)
        {
            System.out.println("You haven't logged into an account yet.");
            existingAccounts();
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("1.Withdrawal\n2.Deposit\n3.Transfer\n4.Check balance\n5.Print transactions\n6.Back");
        String choice = scanner.nextLine();
        switch (choice)
        {
            case "1" -> withdrawal(getAmount());
            case "2" -> deposit(getAmount());
            case "3" -> transfer();
            case "4" -> {
                currentUser.checkBalance(currentAccount);
                loggedInAccountMenu();
            }
            case "5" -> {
                currentAccount.printAccountData();
                System.out.println();
                currentAccount.printTransactions();
                System.out.println();
                loggedInAccountMenu();
            }
            case "6" -> {
                currentAccount = null;
                existingAccounts();
            }
        }
    }

    public int getAmount()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("How much money?");
        return Integer.parseInt(scanner.nextLine());
    }

    public void withdrawal(int amount)
    {
        currentUser.withdrawal(currentAccount, amount);
        // go back
        loggedInAccountMenu();
    }

    public void deposit(int amount)
    {
        currentUser.deposit(currentAccount, amount);
        // go back
        loggedInAccountMenu();
    }

    public void transfer()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter destination account serial number & the amount of money to transfer(serial amount)");

        String[] result = scanner.nextLine().split("\\s");
        // CHECK IF RESULT[0] IS VALID FOR UUID, (ILLEGAL ARGUMENT EXCEPTION)
        UUID serial = UUID.fromString(result[0]);
        int amount = Integer.parseInt(result[1]);

        Account destAccount = bank.findAccount(serial);
        currentUser.transfer(currentAccount, destAccount, amount);
        // go back
        loggedInAccountMenu();
    }

    public void sysadminLogIn()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1.Log in as sysadmin\n2.Back");
        if (scanner.nextLine().equals("2"))
            mainMenu();

        System.out.println("Enter userID and password");
        String username = scanner.nextLine();
        String password = scanner.nextLine();
        if (username.equals("sysadmin") && password.equals("1234"))
        {
            System.out.println("Logged in as sysadmin.");
            sysadmin = true;
            sysadminMenu();
        }
        else
        {
            System.out.println("Username or password is incorrect.");
            sysadminLogIn();
        }
    }

    public void sysadminMenu()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1.Display users\n2.Display accounts\n3.Remove user\n4.Remove account\n5.Back");
        switch (scanner.nextLine()) {
            case "1" -> {
                bank.displayUsers();
                sysadminMenu();
            }
            case "2" -> {
                bank.displayAccounts();
                sysadminMenu();
            }
            case "3" -> removeUser();
            case "4" -> removeAccount();
            case "5" -> sysadminLogIn();
            default -> sysadminMenu();
        }
    }

    public void removeUser()
    {
        if (!sysadmin)
        {
            System.out.println("System admin has not logged in yet.");
            sysadminLogIn();
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the user's ID:");
        String ID = scanner.nextLine();
        User user = bank.findUser(ID);

        if(!bank.removeUser(user))
        {
            System.out.println("1.Try again\n2.Back");
            if (scanner.nextLine().equals("1"))
                removeUser();
            else
                sysadminMenu();
        }
        sysadminMenu();
    }

    public void removeAccount()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the account's serial number:");
        UUID serial = UUID.fromString(scanner.nextLine());
        Account account = bank.findAccount(serial);

        // remove the account from both the bank's list of account and the owner's list of account
        if (!bank.removeAccount(account))
        {
            System.out.println("1.Try again\n2.Back");
            if (scanner.nextLine().equals("1"))
                removeAccount();
            else
                sysadminMenu();
        }
        sysadminMenu();
    }

    public Main()
    {
        bank = new BankingSystem();
        currentUser = null;
        currentAccount = null;
        mainMenu();
    }

    public static void main(String[] args)
    {
        new Main();
    }
}
