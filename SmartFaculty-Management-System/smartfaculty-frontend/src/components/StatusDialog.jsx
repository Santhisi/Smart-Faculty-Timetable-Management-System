import "./StatusDialog.css";

export default function StatusDialog({
    type,
    message,
    onClose
}) {
    if (!message) return null;

    const isSuccess = type === "success";

    const displayMessage =
        typeof message === "string"
            ? message
            : JSON.stringify(message);

    return (
        <div className="custom-dialog-overlay">

            <div className="custom-dialog">

                <div
                    className={`dialog-icon ${
                        isSuccess
                            ? "success-icon"
                            : "error-icon"
                    }`}
                >
                    {isSuccess ? "✓" : "!"}
                </div>

                <h2>
                    {isSuccess ? "Success" : "Operation Failed"}
                </h2>

                <p>{displayMessage}</p>

                <div className="dialog-buttons">

                    <button
                        type="button"
                        className={
                            isSuccess
                                ? "status-ok success-button"
                                : "status-ok error-button"
                        }
                        onClick={onClose}
                    >
                        OK
                    </button>

                </div>

            </div>

        </div>
    );
}