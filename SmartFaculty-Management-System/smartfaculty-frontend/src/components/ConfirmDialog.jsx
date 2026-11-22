import { useRef, useState } from "react";
import "./ConfirmDialog.css";

export function useConfirmDialog() {
    const [open, setOpen] = useState(false);
    const [message, setMessage] = useState("");
    const resolver = useRef(null);

    const confirm = (text) => {
        setMessage(text);
        setOpen(true);

        return new Promise((resolve) => {
            resolver.current = resolve;
        });
    };

    const handleConfirm = () => {
        setOpen(false);
        resolver.current?.(true);
        resolver.current = null;
    };

    const handleCancel = () => {
        setOpen(false);
        resolver.current?.(false);
        resolver.current = null;
    };

    return {
        confirm,
        dialogProps: {
            open,
            message,
            onConfirm: handleConfirm,
            onCancel: handleCancel
        }
    };
}

export default function ConfirmDialog({
    open,
    message,
    onConfirm,
    onCancel
}) {
    if (!open) return null;

    return (
        <div className="custom-dialog-overlay">
            <div className="custom-dialog">

                <div className="dialog-icon warning-icon">
                    !
                </div>

                <h2>Confirm Action</h2>

                <p>{message}</p>

                <div className="dialog-buttons">

                    <button
                        type="button"
                        className="dialog-cancel"
                        onClick={onCancel}
                    >
                        Cancel
                    </button>

                    <button
                        type="button"
                        className="dialog-confirm"
                        onClick={onConfirm}
                    >
                        Confirm
                    </button>

                </div>

            </div>
        </div>
    );
}