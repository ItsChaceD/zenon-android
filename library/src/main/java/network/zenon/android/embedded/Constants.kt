package network.zenon.android.embedded

import network.zenon.android.utils.ONE_QSR
import network.zenon.android.utils.ONE_ZNN

const val GENESIS_TIMESTAMP = 1637755200

// Plasma
const val FUSE_MIN_QSR_AMOUNT = 10 * ONE_QSR
const val MIN_PLASMA_AMOUNT = 21000

// Pillar
const val PILLAR_REGISTER_ZNN_AMOUNT = 15000 * ONE_ZNN
const val PILLAR_REGISTER_QSR_AMOUNT = 150000 * ONE_QSR
const val PILLAR_NAME_MAX_LENGTH = 40
val PILLAR_NAME_REG_EXP = Regex("""^([a-zA-Z0-9]+[-._]?)*[a-zA-Z0-9]$""")

// Sentinel
const val SENTINEL_REGISTER_ZNN_AMOUNT = 5000 * ONE_ZNN
const val SENTINEL_REGISTER_QSR_AMOUNT = 50000 * ONE_QSR

// Staking
const val STAKE_MIN_ZNN_AMOUNT = ONE_ZNN
const val STAKE_TIME_UNIT_SEC = 30 * 24 * 60 * 60
const val STAKE_TIME_MAX_SEC = 12 * STAKE_TIME_UNIT_SEC
const val STAKE_UNIT_DURATION_NAME = "month"

// Token
const val TOKEN_ZTS_ISSUE_FEE_IN_ZNN = ONE_ZNN
const val TOKEN_NAME_MAX_LENGTH = 40
const val TOKEN_SYMBOL_MAX_LENGTH = 10
val TOKEN_SYMBOL_EXCEPTIONS = arrayOf("ZNN", "QSR")
val TOKEN_NAME_REG_EXP = Regex("""^([a-zA-Z0-9]+[-._]?)*[a-zA-Z0-9]$""")
val TOKEN_SYMBOL_REG_EXP = Regex("""^[A-Z0-9]+$""")
val TOKEN_DOMAIN_REG_EXP = Regex("""^([A-Za-z0-9][A-Za-z0-9-]{0,61}[A-Za-z0-9]\.)+[A-Za-z]{2,}$""")

// Accelerator
const val PROJECT_DESCRIPTION_MAX_LENGTH = 240
const val PROJECT_NAME_MAX_LENGTH = 30
const val PROJECT_CREATION_FEE_IN_ZNN = 1
const val PROJECT_VOTING_STATUS = 0
const val PROJECT_ACTIVE_STATUS = 1
const val PROJECT_PAID_STATUS = 2
const val PROJECT_CLOSED_STATUS = 3
val PROJECT_URL_REG_EXP = Regex("""^[a-zA-Z0-9]{2,60}\.[a-zA-Z]{1,6}([a-zA-Z0-9()@:%_\\+.~#?&/=-]{0,100})$""")

// Swap
const val SWAP_ASSET_DECAY_TIMESTAMP_START = 1645531200
const val SWAP_ASSET_DECAY_EPOCHS_OFFSET = 30 * 3
const val SWAP_ASSET_DECAY_TICK_EPOCHS = 30
const val SWAP_ASSET_DECAY_TICK_VALUE_PERCENTAGE = 10