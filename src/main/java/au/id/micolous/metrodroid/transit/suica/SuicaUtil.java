/*
 * SuicaTransitData.java
 *
 * Based on code from http://code.google.com/p/nfc-felica/
 * nfc-felica by Kazzz. See project URL for complete author information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Thanks to these resources for providing additional information about the Suica format:
 * http://www.denno.net/SFCardFan/
 * http://jennychan.web.fc2.com/format/suica.html
 * http://d.hatena.ne.jp/baroqueworksdev/20110206/1297001722
 * http://handasse.blogspot.com/2008/04/python-pasorisuica.html
 * http://sourceforge.jp/projects/felicalib/wiki/suica
 *
 * Some of these resources have been translated into English at:
 * https://github.com/micolous/metrodroid/wiki/Suica
 */
package au.id.micolous.metrodroid.transit.suica;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import au.id.micolous.metrodroid.transit.Station;

import net.kazzz.felica.lib.Util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import au.id.micolous.farebot.R;
import au.id.micolous.metrodroid.MetrodroidApplication;

final class SuicaUtil {
    private static final String TAG = "SuicaUtil";

    private SuicaUtil() {
    }

    static Calendar extractDate(boolean isProductSale, byte[] data) {
        int date = Util.toInt(data[4], data[5]);
        if (date == 0)
            return null;
        int yy = date >> 9;
        int mm = (date >> 5) & 0xf;
        int dd = date & 0x1f;
        Calendar c = GregorianCalendar.getInstance();
        c.set(Calendar.YEAR, 2000 + yy);
        c.set(Calendar.MONTH, mm - 1);
        c.set(Calendar.DAY_OF_MONTH, dd);

        // Product sales have time, too.
        // 物販だったら時s間もセット
        if (isProductSale) {
            int time = Util.toInt(data[6], data[7]);
            int hh = time >> 11;
            int min = (time >> 5) & 0x3f;
            c.set(Calendar.HOUR_OF_DAY, hh);
            c.set(Calendar.MINUTE, min);
        } else {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
        }
        return c;
    }

    /**
     * 機器種別を取得します
     * <pre>http://sourceforge.jp/projects/felicalib/wiki/suicaを参考にしています</pre>
     *
     * @param cType コンソールタイプをセット
     * @return String 機器タイプが文字列で戻ります
     */
    static String getConsoleTypeName(int cType) {
        Application app = MetrodroidApplication.getInstance();
        switch (cType & 0xff) {
            case 0x03:
                return app.getString(R.string.felica_terminal_fare_adjustment);
            case 0x04:
                return app.getString(R.string.felica_terminal_portable);
            case 0x05:
                return app.getString(R.string.felica_terminal_vehicle); // bus
            case 0x07:
                return app.getString(R.string.felica_terminal_ticket);
            case 0x08:
                return app.getString(R.string.felica_terminal_ticket);
            case 0x09:
                return app.getString(R.string.felica_terminal_deposit_quick_charge);
            case 0x12:
                return app.getString(R.string.felica_terminal_tvm_tokyo_monorail);
            case 0x13:
                return app.getString(R.string.felica_terminal_tvm_etc);
            case 0x14:
                return app.getString(R.string.felica_terminal_tvm_etc);
            case 0x15:
                return app.getString(R.string.felica_terminal_tvm_etc);
            case 0x16:
                return app.getString(R.string.felica_terminal_turnstile);
            case 0x17:
                return app.getString(R.string.felica_terminal_ticket_validator);
            case 0x18:
                return app.getString(R.string.felica_terminal_ticket_booth);
            case 0x19:
                return app.getString(R.string.felica_terminal_ticket_office_green);
            case 0x1a:
                return app.getString(R.string.felica_terminal_ticket_gate_terminal);
            case 0x1b:
                return app.getString(R.string.felica_terminal_mobile_phone);
            case 0x1c:
                return app.getString(R.string.felica_terminal_connection_adjustment);
            case 0x1d:
                return app.getString(R.string.felica_terminal_transfer_adjustment);
            case 0x1f:
                return app.getString(R.string.felica_terminal_simple_deposit);
            case 0x46:
                return "VIEW ALTTE";
            case 0x48:
                return "VIEW ALTTE";
            case 0xc7:
                return app.getString(R.string.felica_terminal_pos);  // sales
            case 0xc8:
                return app.getString(R.string.felica_terminal_vending);   // sales
            default:
                return String.format("Console 0x%s", Integer.toHexString(cType));
        }
    }

    /**
     * 処理種別を取得します
     * <pre>http:// sourceforge.jp/projects/felicalib/wiki/suicaを参考にしています</pre>
     *
     * @param proc 処理タイプをセット
     * @return String 処理タイプが文字列で戻ります
     */
    static String getProcessTypeName(int proc) {
        Application app = MetrodroidApplication.getInstance();
        switch (proc & 0xff) {
            case 0x01:
                return app.getString(R.string.felica_process_fare_exit_gate);
            case 0x02:
                return app.getString(R.string.felica_process_charge);
            case 0x03:
                return app.getString(R.string.felica_process_purchase_magnetic);
            case 0x04:
                return app.getString(R.string.felica_process_fare_adjustment);
            case 0x05:
                return app.getString(R.string.felica_process_admission_payment);
            case 0x06:
                return app.getString(R.string.felica_process_booth_exit);
            case 0x07:
                return app.getString(R.string.felica_process_issue_new);
            case 0x08:
                return app.getString(R.string.felica_process_booth_deduction);
            case 0x0d:
                return app.getString(R.string.felica_process_bus_pitapa);                 // Bus
            case 0x0f:
                return app.getString(R.string.felica_process_bus_iruca);                  // Bus
            case 0x11:
                return app.getString(R.string.felica_process_reissue);
            case 0x13:
                return app.getString(R.string.felica_process_payment_shinkansen);
            case 0x14:
                return app.getString(R.string.felica_process_entry_a_autocharge);
            case 0x15:
                return app.getString(R.string.felica_process_exit_a_autocharge);
            case 0x1f:
                return app.getString(R.string.felica_process_deposit_bus);                // Bus
            case 0x23:
                return app.getString(R.string.felica_process_purchase_special_ticket);    // Bus
            case 0x46:
                return app.getString(R.string.felica_process_merchandise_purchase);       // Sales
            case 0x48:
                return app.getString(R.string.felica_process_bonus_charge);
            case 0x49:
                return app.getString(R.string.felica_process_register_deposit);           // Sales
            case 0x4a:
                return app.getString(R.string.felica_process_merchandise_cancel);         // Sales
            case 0x4b:
                return app.getString(R.string.felica_process_merchandise_admission);      // Sales
            case 0xc6:
                return app.getString(R.string.felica_process_merchandise_purchase_cash);  // Sales
            case 0xcb:
                return app.getString(R.string.felica_process_merchandise_admission_cash); // Sales
            case 0x84:
                return app.getString(R.string.felica_process_payment_thirdparty);
            case 0x85:
                return app.getString(R.string.felica_process_admission_thirdparty);
            default:
                return String.format("Process0x%s", Integer.toHexString(proc));
        }
    }

    /**
     * Gets bus stop information from the IruCa (イルカ) table.
     *
     * @param lineCode    Bus line ID (line code)
     * @param stationCode Bus stop ID (station code)
     * @return If the stop is known, a Station is returned describing it. If the stop is unknown,
     *         or there was some other database error, null is returned.
     */
    static Station getBusStop(int regionCode, int lineCode, int stationCode) {
        int areaCode = (regionCode >> 6);

        try {
            SQLiteDatabase db = MetrodroidApplication.getInstance().getSuicaDBUtil().openDatabase();
            Cursor cursor = db.query(SuicaDBUtil.TABLE_IRUCA_STATIONCODE,
                    SuicaDBUtil.COLUMNS_IRUCA_STATIONCODE,
                    String.format("%s = ? AND %s = ?", SuicaDBUtil.COLUMN_LINECODE, SuicaDBUtil.COLUMN_STATIONCODE),
                    new String[]{Integer.toHexString(lineCode), Integer.toHexString(stationCode)},
                    null,
                    null,
                    SuicaDBUtil.COLUMN_ID);

            if (!cursor.moveToFirst()) {
                return null;
            }

            // FIXME: Figure out a better way to deal with i18n.
            boolean isJa = Locale.getDefault().getLanguage().equals("ja");
            String companyName = cursor.getString(cursor.getColumnIndex(isJa ? SuicaDBUtil.COLUMN_COMPANYNAME : SuicaDBUtil.COLUMN_COMPANYNAME_EN));
            String stationName = cursor.getString(cursor.getColumnIndex(isJa ? SuicaDBUtil.COLUMN_STATIONNAME : SuicaDBUtil.COLUMN_STATIONNAME_EN));
            return new Station(companyName, null, stationName, null, null, null);

        } catch (Exception e) {
            Log.e(TAG, "getBusStop() error", e);
            return null;
        }
    }

    /**
     * Gets train station information from the Japan Rail (JR) table.
     *
     * @param regionCode  Train area/region ID (region code)
     * @param lineCode    Train line ID (line code)
     * @param stationCode Train station ID (station code)
     * @return If the stop is known, a Station is returned describing it. If the stop is unknown,
     *         or there was some other database error, null is returned.
     */
    static Station getRailStation(int regionCode, int lineCode, int stationCode) {
        int areaCode = (regionCode >> 6);

        try {
            SQLiteDatabase db = MetrodroidApplication.getInstance().getSuicaDBUtil().openDatabase();
            Cursor cursor = db.query(
                    SuicaDBUtil.TABLE_STATIONCODE,
                    SuicaDBUtil.COLUMNS_STATIONCODE,
                    String.format("%s = ? AND %s = ? AND %s = ?", SuicaDBUtil.COLUMN_AREACODE, SuicaDBUtil.COLUMN_LINECODE, SuicaDBUtil.COLUMN_STATIONCODE),
                    new String[]{
                            String.valueOf(areaCode & 0xFF),
                            String.valueOf(lineCode & 0xFF),
                            String.valueOf(stationCode & 0xFF)
                    },
                    null,
                    null,
                    SuicaDBUtil.COLUMN_ID);

            if (!cursor.moveToFirst()) {
                Log.w(TAG, String.format("FAILED get rail company: r: 0x%s a: 0x%s l: 0x%s s: 0x%s",
                        Integer.toHexString(regionCode),
                        Integer.toHexString(areaCode),
                        Integer.toHexString(lineCode),
                        Integer.toHexString(stationCode)));

                return null;
            }

            // FIXME: Figure out a better way to deal with i18n.
            boolean isJa = Locale.getDefault().getLanguage().equals("ja");
            String companyName = cursor.getString(cursor.getColumnIndex(isJa ? SuicaDBUtil.COLUMN_COMPANYNAME : SuicaDBUtil.COLUMN_COMPANYNAME_EN));
            String lineName = cursor.getString(cursor.getColumnIndex(isJa ? SuicaDBUtil.COLUMN_LINENAME : SuicaDBUtil.COLUMN_LINENAME_EN));
            String stationName = cursor.getString(cursor.getColumnIndex(isJa ? SuicaDBUtil.COLUMN_STATIONNAME : SuicaDBUtil.COLUMN_STATIONNAME_EN));
            String latitude = cursor.getString(cursor.getColumnIndex(SuicaDBUtil.COLUMN_LATITUDE));
            String longitude = cursor.getString(cursor.getColumnIndex(SuicaDBUtil.COLUMN_LONGITUDE));
            return new Station(companyName, lineName, stationName, null, latitude, longitude);

        } catch (Exception e) {
            Log.e(TAG, "Error in getRailStation", e);
            return null;
        }
    }
}
