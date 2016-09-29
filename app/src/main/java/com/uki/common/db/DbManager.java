package com.uki.common.db;

import java.io.File;

import com.uki.common.db.provider.DbProvider;

public interface DbManager
{
	void deploy();

	void deleteDb();

	boolean exists();

	void setDbProvider(DbProvider dbProvider);

	void backup();
	
	File getBackupFile();
}