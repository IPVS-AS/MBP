import threading
import time
import tkinter

from pymongo import MongoClient

from rmpdiscovery.discovery import discoveryconst as const

LABEL_COLUMN = 0
LABEL_ROW = 0
LABEL_COLUMN_SPAN = 3
LIST_COLUMN = 0
LIST_ROW = 1
LIST_COLUMN_SPAN = 3
BTN_STOP_COLUMN = 1
BTN_STOP_ROW = 2


class simpleapp_tk(tkinter.Tk):
    def __init__(self, parent):
        tkinter.Tk.__init__(self, parent)

        self.db_client = MongoClient()
        self.dev_coll = self.db_client[const.DISCOVERY_DB_NAME][const.DEV_COLL_NAME]
        self.mon_coll = self.db_client[const.DISCOVERY_DB_NAME][const.MONITOR_COLL_NAME]

        self.reload = False

        self.lbl_devices = None
        self.lbl_devices_text = None
        self.list_device = None
        self.btn_stop = None
        self.parent = parent
        self.initialize()

    def initialize(self):
        self.grid()

        self.lbl_devices_text = tkinter.StringVar()
        self.lbl_devices = tkinter.Label(self, textvariable=self.lbl_devices_text)
        self.lbl_devices.grid(column=LABEL_COLUMN, row=LABEL_ROW, columnspan=LABEL_COLUMN_SPAN)
        self.lbl_devices_text.set('Devices:\nGLOBAL_ID, LOCAL_ID, TYPE, IP, HW_ADDR, HOST')

        self.list_device = tkinter.Listbox(self)
        self.list_device.grid(column=LIST_COLUMN, row=LIST_ROW, columnspan=LIST_COLUMN_SPAN)
        self.list_device.config(width=0)

        self.btn_stop = tkinter.Button(self, text="Start", command=self.OnStart)
        self.btn_stop.grid(column=BTN_STOP_COLUMN, row=BTN_STOP_ROW)

        self.grid_columnconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=1)
        self.grid_columnconfigure(2, weight=1)

        # self.entryVariable = tkinter.StringVar()
        # self.entry = tkinter.Entry(self, textvariable=self.entryVariable)
        # self.entry.grid(column=0, row=0, sticky='EW')
        # self.entry.bind("<Return>", self.OnPressEnter)
        # self.entryVariable.set(u"Enter text here.")
        #
        # button = tkinter.Button(self, text=u"Click me !",
        #                         command=self.OnStart)
        # button.grid(column=1, row=0)
        #
        # self.labelVariable = tkinter.StringVar()
        # label = tkinter.Label(self, textvariable=self.labelVariable,
        #                       anchor="w", fg="white", bg="blue")
        # label.grid(column=0, row=1, columnspan=2, sticky='EW')
        # self.labelVariable.set(u"Hello !")
        #
        # self.grid_columnconfigure(0, weight=1)
        # self.resizable(True, False)
        # self.update()
        # self.geometry(self.geometry())
        # self.entry.focus_set()
        # self.entry.selection_range(0, tkinter.END)

    def OnStart(self):
        self.reload = True
        self.btn_stop.config(text='Stop')
        self.btn_stop.config(command=self.OnStop)
        thread = threading.Thread(target=self.keep_uptodate)
        thread.start()

    def OnStop(self):
        self.btn_stop.config(text='Start')
        self.btn_stop.config(command=self.OnStart)
        self.reload = False

    def set_devices(self, devices):
        self.list_device.delete(0, tkinter.END)
        device_strings = [format_device(device) for device in devices]
        [self.list_device.insert(tkinter.END, dev_str) for dev_str in device_strings]

    def fetch_devices(self):
        monitored_devices = list(self.mon_coll.find({}))
        global_ids = [mon_dev[const.GLOBAL_ID] for mon_dev in monitored_devices]
        device_details = list(self.dev_coll.find({const.GLOBAL_ID: {'$in': global_ids}}))
        return device_details

    def keep_uptodate(self):
        while self.reload:
            self.set_devices(self.fetch_devices())
            time.sleep(const.SERVER_MONITOR_SLEEP)


def format_device(device):
    if const.GLOBAL_ID not in device:
        raise ValueError('No GLOBAL_ID')
    if const.LOCAL_ID not in device:
        raise ValueError('No LOCAL_ID')
    if const.DEV_HW_ADDRESS not in device:
        raise ValueError('No hardware address')
    if const.DEV_TYPE not in device:
        raise ValueError('No type')

    device_string = str(device[const.GLOBAL_ID]) + '\t' + device[const.LOCAL_ID] + '\t' + device[const.DEV_TYPE] + '\t'

    if const.DEV_IP in device and device[const.DEV_IP] is not None:
        device_string += device[const.DEV_IP]

    device_string += '\t' + device[const.DEV_HW_ADDRESS] + '\t'

    if const.HOST in device and device[const.HOST] is not None:
        device_string += str(device[const.HOST])

    device_string += '\t'

    return device_string


if __name__ == "__main__":
    app = simpleapp_tk(None)
    app.title('Connde discovery')
    app.mainloop()
