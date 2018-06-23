package party.liyin.socketchannel.callback;

import party.liyin.socketchannel.exception.OperationNotSupportException;
import party.liyin.socketchannel.transporthelper.FileTrasportHelper;

import java.io.File;
import java.io.IOException;

public class FileTask {
    FileTrasportHelper.FileTransportBean bean;
    private boolean taskIsDone = false;

    FileTask(FileTrasportHelper.FileTransportBean bean) {
        this.bean = bean;
    }

    /**
     * Save file from FileArrived Callback
     *
     * @param output file to save
     * @throws IOException
     */
    public void save(File output) throws IOException {
        if (taskIsDone) throw new OperationNotSupportException("You can not run a task which is done.");
        FileTrasportHelper.saveFileFromBean(this.bean, output);
    }

    /**
     * Get file byte array. Warning: this method can not work with large file.
     *
     * @return Byte Array
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        if (taskIsDone) throw new OperationNotSupportException("You can not run a task which is done.");
        return FileTrasportHelper.getByteArrayFromBean(bean);
    }
}