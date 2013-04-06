package com.earth2me.minecraft.forgeplugin.util;


public final class Strings
{
	private Strings()
	{
	}
	
	public String join(final String delimiter, final String... args)
	{
		if (args == null || args.length < 1)
		{
			return "";
		}
		
		int length = delimiter.length() * (args.length - 1);
		for (String string : args)
		{
			length += string.length();
		}
		
		final StringBuilder builder = new StringBuilder(length);
		builder.append(args[0]);
		for (int i = 1; i < args.length; i++)
		{
			builder.append(delimiter).append(args[i]);
		}
		
		return builder.toString().intern();
	}
}
