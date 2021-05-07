package life.qbic.datasets.parsers

/**
 * A parser that takes an input data structure and tries to convert it to a certain type dataset
 * of type T.
 *
 * The user of this interface can expect a valid dataset of type T if no exceptions have
 * been thrown after parsing of data structure has been called.
 *
 * @since 1.7.0
 */
interface DataSetParser<T> {

    /**
     * Parses and validates a data structure from a given path of a directory in the filesystem.
     * <p> As an example, for a data set such as this:
     * <pre>
     * - /SomePath/MyDataset
     *      | - myFile.txt
     *      ` - anotherFile.txt
     * </pre>
     * you would need to provide <code>"/SomePath/MyDataset"</code> as path.
     *
     * @param rootPath The root path of the dataset structure, represents the top level of the
     * hierarchical data set structure. This path must be absolute.
     * @return A successfully parsed and validated dataset
     * @throws DataParserException if the data structure cannot be parsed (unknown structure)
     * @throws DataSetValidationException if the data structure does not match a predefined schema
     * @since 1.7.0
     */
    T parseFrom(String rootPath) throws DataParserException, DataSetValidationException

}
