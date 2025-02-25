package ${objectMap.modulePackage}.entity;

import java.time.LocalDateTime;
import java.io.Serializable;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;
import org.sagacity.sqltoy.config.annotation.Column;

/**
* @author ${objectMap.author}
* Table: ${objectMap.table.tableName},Remark:${objectMap.table.tableComment}
*/
public class ${objectMap.table.javaClassName} implements Serializable {
	private static final long serialVersionUID = 1L;

	<#list objectMap.colList as item>
	/**
	* ${item.columnComment}
	*/
	private ${item.javaDataType} ${item.javaColName};

	</#list>

	<#list objectMap.colList as item>
	public ${item.javaDataType} get${item.javaColNameBigHump}() {
		return this.${item.javaColName};
	}

	public ${objectMap.table.javaClassName} set${item.javaColNameBigHump}(${item.javaDataType} ${item.javaColName}) {
		this.${item.javaColName}=${item.javaColName};
		return this;
	}

	</#list>

	@Override
	public String toString() {
		StringBuilder columnsBuffer = new StringBuilder();
	<#list objectMap.colList as item>
		columnsBuffer.append("${item.javaColName}=").append(get${item.javaColNameBigHump}()).append("\n");
	</#list>
		return columnsBuffer.toString();
	}
}
