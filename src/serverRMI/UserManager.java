package serverRMI;

import common.rmi.ExistingUserException;
import common.rmi.RemoteUserManager;
import common.rmi.UserAuthenticationException;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/13/13
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserManager extends UnicastRemoteObject implements RemoteUserManager
{
	protected final int startCash = 1000;

	public UserManager() throws RemoteException
	{
		super();
	}

	/**
	 * Authenticates user. If the user credentials are wrong throws UserAuthenticationException.
	 * @param name The username of the user to authenticate.
	 * @param pass The password of the user to authenticate.
	 * @throws RemoteException
	 * @throws UserAuthenticationException
	 * @throws SQLException
	 */
	public int authenticate(String name, String pass) throws RemoteException, UserAuthenticationException, SQLException
	{

        Connection db = ServerRMI.pool.connectionCheck();

		int tries = 0, maxTries = 3;

		PreparedStatement queryUser = null;
		ResultSet resultSet = null;

		String query = "SELECT id FROM sduser WHERE username LIKE ? AND password LIKE ?";

		while(tries < maxTries)
		{
			try {
				queryUser = db.prepareStatement(query);
				queryUser.setString(1, name);
				queryUser.setString(2, hashPassword(pass));

				resultSet = queryUser.executeQuery();

				if(!resultSet.next())
				{
					//User password combination does not exist.
					throw new UserAuthenticationException();
				}

				return resultSet.getInt("id");
			} catch (SQLException e) {
				System.out.println(e);
				if(tries++ > maxTries)
					throw e;
			} finally {
				if(queryUser != null)
					queryUser.close();
			}
		}

		ServerRMI.pool.releaseConnection(db);

		return -1;
	}

	/**
	 * Registers a new user. If desired username is in use throws ExistingUserException
	 * @param name  The desired password of the user to register.
	 * @param pass  The desired password of the user to register.
	 * @throws RemoteException
	 * @throws ExistingUserException
	 * @throws SQLException
	 */
	public void register(String name, String pass, String nameAlias) throws RemoteException, ExistingUserException, SQLException
	{
        Connection db = ServerRMI.pool.connectionCheck();

		int tries = 0, maxTries = 3;

		PreparedStatement queryUser = null;
		PreparedStatement insertUser = null;
		ResultSet resultSet = null;

		String query = "SELECT id FROM sduser WHERE username LIKE ?";
		String insert = "INSERT INTO sduser (id, username, password, namealias, cash) VALUES(sduser_id_inc.nextval,?,?,?,?)";

		//See if username is already in use.
		while(tries < maxTries)
		{
			try {
				queryUser = db.prepareStatement(query);
				queryUser.setString(1, name);

				resultSet = queryUser.executeQuery();

				if(resultSet.next())
					throw new ExistingUserException();

				break;
			} catch (SQLException e) {
				System.out.println("\n"+e+"\n");
				if(tries++ > maxTries)
					throw e;
			} finally {
				if(queryUser != null)
					queryUser.close();
			}
		}

		//If execution reaches this point, insert new user.
		try {
			db.setAutoCommit(false);

			insertUser = db.prepareStatement(insert);
			insertUser.setString(1, name);
			insertUser.setString(2, hashPassword(pass));
			insertUser.setString(3, nameAlias);
			insertUser.setInt(4, startCash);

			insertUser.executeQuery();

			db.commit();
		} catch (SQLException e) {
			System.out.println("\n"+e+"\n");
			if(db != null)
				db.rollback();
			throw e;
		} finally {
			if(insertUser != null)
				insertUser.close();

			db.setAutoCommit(true);
		}

		ServerRMI.pool.releaseConnection(db);
	}

	/**
	 * Hashes the password using MD5 and returns it.
	 * @param pass The plaintext password to be hashed.
	 * @return The hashed password.
	 */
	protected String hashPassword(String pass)
	{
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Cannot find hashing algorithm:\n" + e);
			System.exit(-1);
		}
		if(m == null)
		{
			System.out.println("Cannot find hashing algorithm.");
			System.exit(-1);
		}

		m.reset();
		m.update(pass.getBytes());

		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashText = bigInt.toString(16);

		while(hashText.length() < 32)
			hashText = "0" + hashText;

		return hashText;
	}
}
