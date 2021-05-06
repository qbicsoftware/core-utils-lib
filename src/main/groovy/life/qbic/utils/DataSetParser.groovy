package life.qbic.utils

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
     * Tries to parse and validate a data structure from a given path on the filesystem.
     *
     * The root path must be absolute!
     *
     * Given a dataset with the structure:
     *
     * - /SomePath/MyDataset
     *      |- myFile.txt
     *      |- anotherFile.txt
     *
     * Then /SomePath/MyDataset would be the root path.
     *
     * @param rootPath The root path of the dataset structure, represents the top level of the
     * hierarchical data set structure.
     * @return A successfully parsed and validated dataset of type T
     * @throws DataParserException if the data structure cannot be parsed (unknown structure)
     * @throws DataSetValidationException if the data structure is missing some required properties
     */
    T parseFrom(String rootPath) throws DataParserException, DataSetValidationException

}
