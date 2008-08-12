package org.rifidi.emulator.reader.thingmagic.commandobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.emulator.reader.thingmagic.commandobjects.exceptions.CommandCreationExeption;
import org.rifidi.emulator.reader.thingmagic.database.IDBRow;
import org.rifidi.emulator.reader.thingmagic.database.IDBTable;
import org.rifidi.emulator.reader.thingmagic.database.impl.tagbuffer.ThingMagicTagTableMemory;
import org.rifidi.emulator.reader.thingmagic.module.ThingMagicReaderSharedResources;

public class SelectCommand implements Command {
	private static Log logger = LogFactory.getLog(SelectCommand.class);

	private String command;

	private List<String> columns = new ArrayList<String>();
	private String table;

	private ThingMagicReaderSharedResources tmsr;

	public SelectCommand(String command, ThingMagicReaderSharedResources tmsr)
			throws CommandCreationExeption {
		this.tmsr = tmsr;
		this.command = command;
		List<String> commandBlocks = new ArrayList<String>();
		// TODO Auto-generated constructor stub

		logger.debug("Parsing command: " + command);

		/*
		 * how goal here is not to do error checking but to parse the incoming
		 * command to make it easer to check for errors latter on as the command
		 * is broken up into very predictable sub-blocks See java.util.regex
		 */
		Pattern pattern = Pattern.compile(
				"\\w+|[\\s,]+|<>|>=|<=|=|>|<|\\(|\\)|[^\\s\\w,<>=\\(\\)]+",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(command.toLowerCase());

		while (matcher.find())
			commandBlocks.add(matcher.group());
		
		logger.debug("Command Blockes: " + commandBlocks);
		
		int index = 0;
		if (commandBlocks.get(index).matches("\\s+")) {
			index++;
		}

		if (!commandBlocks.get(index).equals("select")) {
			// TODO throw an exception
		}
		index++;

		if (!commandBlocks.get(index).matches("\\s+")) {
			// TODO throw an exception
		}

		index++;

		for (; !commandBlocks.get(index).equals("from"); index++) {
			if (commandBlocks.get(index).matches("[,\\s]+"))
				continue;
			if (commandBlocks.get(index).matches("\\w+")) {
				columns.add(commandBlocks.get(index));
			} else {
				// TODO throw an exception
			}
		}
		index++;

		if (!commandBlocks.get(index).matches("\\s+")) {
			// TODO throw an exception
		}
		index++;

		if (commandBlocks.get(index).matches("\\w+")) {
			table = commandBlocks.get(index);
		} else {
			// TODO throw an exception
		}

		index++;
		if (commandBlocks.size() < index) {
			if (!commandBlocks.get(index).matches("\\s+")) {
				// TODO throw an exception
			} else {
				index++;

				if ((commandBlocks.size() < index)
						&& (commandBlocks.get(index).matches("where"))) {
					if (!commandBlocks.get(index).matches("\\s+")) {
						// TODO throw an exception
					}
					index++;

					for (; index < commandBlocks.size(); index++) {
						if (commandBlocks.get(index).equals("set"))
							break;
						// TODO parse where clause
						// whereClause.append(args.get(index));
					}
				}

				if ((commandBlocks.size() < index)
						&& (commandBlocks.get(index).matches("set"))) {
					// TODO implement the set clause
				}
			}
		}

		IDBTable tableImpl = tmsr.getDataBase().getTable(table);
		if (tableImpl == null) {
			throw new CommandCreationExeption(
					"Error 0100:     syntax error at '" + table + "'");
		}

		/*
		 * This is an exception to the rule for the thingmagic reader. If we are
		 * reading a tag... make best effort to complete the command in an
		 * orderly and predictable manner.
		 */
		if (tableImpl instanceof ThingMagicTagTableMemory) {
			return;
		}

		for (int x = 0; x < tableImpl.size(); x++) {
			IDBRow row = tableImpl.get(x);
			for (String column : columns) {
				if (!row.containsColumn(column)) {
					throw new CommandCreationExeption(
							"Error 0100:     Unknown " + table);
				}

				if (!row.isReadable(column)) {
					throw new CommandCreationExeption(
							"Error 0100:     Could not read from '" + column
									+ "' in '" + table + "'");
				}
			}
		}
	}

	@Override
	public ArrayList<Object> execute() {
		// TODO Auto-generated method stub
		ArrayList<Object> retVal = new ArrayList<Object>();
		// TODO add filtering
		logger.debug("Getting table: " + table);
		logger.debug("Getting column data: " + columns);

		for (int x = 0; x < tmsr.getDataBase().getTable(table).size(); x++) {
			IDBRow row = tmsr.getDataBase().getTable(table).get(x);

			StringBuffer buff = new StringBuffer();
			for (int y = 0; y < columns.size(); y++) {
				buff.append(row.get(columns.get(y)));
				if (y < columns.size() - 1)
					buff.append("|");
			}
			retVal.add(buff.toString());
		}
		return retVal;
	}

	@Override
	public String toCommandString() {
		// TODO Auto-generated method stub
		return command;
	}

}
