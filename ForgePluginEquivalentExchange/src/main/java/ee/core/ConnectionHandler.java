package ee.core;

import ee.EEBase;
import forge.IConnectionHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet1Login;


public final class ConnectionHandler implements IConnectionHandler
{

	public ConnectionHandler()
	{
	}

	@Override
	public void onConnect(final NetworkManager nm)
	{
	}

	@Override
	public void onLogin(final NetworkManager nm, final Packet1Login pl)
	{
	}

	@Override
	public void onDisconnect(final NetworkManager nm, final String string, final Object[] os)
	{
		try
		{
			EEBase.cleanup();
		}
		catch (Throwable ex)
		{
			System.out.printf("Error handling onDisconnect in EquivalentExchange:%n%s%n", ex);
		}
	}
}
